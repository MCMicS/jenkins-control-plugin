package org.codinjutsu.tools.jenkins;

import com.intellij.ide.DataManager;
import com.intellij.ide.util.treeView.TreeState;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.val;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildParameter;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.*;
import org.codinjutsu.tools.jenkins.view.action.JobAction;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.*;
import java.util.stream.Stream;

public class JenkinsTree implements PersistentStateComponent<JenkinsTreeState> {
    private static final String LOADING = "Loading...";
    private static final String UNAVAILABLE = "No Jenkins server available";
    private static final TreeState NO_TREE_STATE = null;
    private static final Logger LOG = Logger.getInstance(JenkinsTree.class);
    private final SimpleTree tree;
    @NotNull
    private JenkinsTreeState state = new JenkinsTreeState();
    @Nullable
    private TreeState lastTreeState = NO_TREE_STATE;
    @Nullable
    private JobClickHandler clickHandler;

    public JenkinsTree(Project project, @NotNull JenkinsSettings jenkinsSettings, Jenkins jenkins) {
        this.tree = new TreeWithoutDefaultSearch();

        this.tree.getEmptyText().setText(LOADING);
        this.tree.setCellRenderer(new JenkinsTreeRenderer(jenkinsSettings::isFavoriteJob,
                BuildStatusEnumRenderer.getInstance(project)));
        this.tree.setName("jobTree");

        GuiUtil.runInSwingThread(() -> {
            this.tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(new JenkinsTreeNode.RootNode(jenkins)), false));
        });
        DataManager.registerDataProvider(this.tree, dataId ->
                PlatformDataKeys.COPY_PROVIDER.is(dataId) ? new JenkinsTreeCopyProvider(this) : null);
    }

    @NotNull
    public static Optional<Job> getJob(TreePath treePath) {
        final Class<JenkinsTreeNode.JobNode> jobNodeClass = JenkinsTreeNode.JobNode.class;
        return getLastSelectedPath(treePath, jobNodeClass).map(JenkinsTreeNode.JobNode::getJob);
    }

    @NotNull
    public static Optional<Job> getJob(@NotNull DefaultMutableTreeNode node) {
        final Class<JenkinsTreeNode.JobNode> jobNodeClass = JenkinsTreeNode.JobNode.class;
        return getNode(node, jobNodeClass).map(JenkinsTreeNode.JobNode::getJob);
    }

    @NotNull
    public static <T> Optional<T> getNode(@NotNull DefaultMutableTreeNode node, @NotNull Class<T> expectedClass) {
        return Optional.ofNullable(node.getUserObject()).filter(expectedClass::isInstance).map(expectedClass::cast);
    }

    @NotNull
    public static <T> Optional<T> getLastSelectedPath(@NotNull TreePath treePath, @NotNull Class<T> expectedClass) {
        final Object node = treePath.getLastPathComponent();
        return Optional.ofNullable(node)
                .filter(DefaultMutableTreeNode.class::isInstance).map(DefaultMutableTreeNode.class::cast)
                .map(DefaultMutableTreeNode::getUserObject)
                .filter(expectedClass::isInstance).map(expectedClass::cast);
    }

    @NotNull
    private static DefaultMutableTreeNode createNode(@NotNull BuildParameter buildParameter) {
        return new DefaultMutableTreeNode(new JenkinsTreeNode.BuildParameterNode(buildParameter), true);
    }

    @NotNull
    private static DefaultMutableTreeNode createNode(Build build) {
        return new DefaultMutableTreeNode(new JenkinsTreeNode.BuildNode(build), true);
    }

    @NotNull
    private static DefaultMutableTreeNode createNode(Job job) {
        boolean allowsChildren = true;
        return new DefaultMutableTreeNode(new JenkinsTreeNode.JobNode(job), allowsChildren);
    }

    @NotNull
    private static Comparator<DefaultMutableTreeNode> wrapJobSorter(Comparator<Job> jobComparator) {
        return (node1, node2) -> {
            final Optional<Job> job1 = JenkinsTree.getJob(node1);
            final Optional<Job> job2 = JenkinsTree.getJob(node2);
            if (job1.isPresent() && job2.isPresent()) {
                return jobComparator.compare(job1.get(), job2.get());
            }
            return 0;
        };
    }

    @NotNull
    public static DefaultMutableTreeNode fillJobTree(@NotNull Job job, @NotNull DefaultMutableTreeNode jobNode) {
        jobNode.removeAllChildren();
        if (job.getJobType().containNestedJobs()) {
            job.getNestedJobs().stream().map(JenkinsTree::createJobTree).forEach(jobNode::add);
        } else {
            job.getLastBuilds().stream().map(JenkinsTree::initBuildNode).forEach(jobNode::add);
        }
        return jobNode;
    }

    @NotNull
    private static DefaultMutableTreeNode initBuildNode(Build build) {
        val buildNode = createNode(build);
        Optional.of(build.getBuildParameterList())
                .ifPresent(buildParameters -> buildParameters.stream()
                        .map(JenkinsTree::createNode)
                        .forEach(buildNode::add)
                );
        return buildNode;
    }

    @NotNull
    private static DefaultMutableTreeNode createJobTree(Job job) {
        return fillJobTree(job, createNode(job));
    }

    @NotNull
    public JComponent asComponent() {
        return getTree();
    }

    public void clear() {
        Optional.ofNullable(getModelRoot()).ifPresent(DefaultMutableTreeNode::removeAllChildren);
        getModel().reload();
    }

    @NotNull
    public SimpleTree getTree() {
        return tree;
    }

    @Nullable
    public DefaultMutableTreeNode getLastSelectedPathComponent() {
        return Optional.ofNullable(tree.getLastSelectedPathComponent())
                .filter(DefaultMutableTreeNode.class::isInstance)
                .map(DefaultMutableTreeNode.class::cast).orElse(null);
    }

    public @NotNull Stream<DefaultMutableTreeNode> getSelectedPathComponents() {
        return Optional.ofNullable(tree.getSelectionPaths())
                .stream().flatMap(Arrays::stream)
                .map(TreePath::getLastPathComponent)
                .filter(DefaultMutableTreeNode.class::isInstance)
                .map(DefaultMutableTreeNode.class::cast);
    }

    @NotNull
    public <T> Optional<T> getLastSelectedPath(@NotNull Class<T> expectedClass) {
        return Optional.ofNullable(getLastSelectedPathComponent())
                .map(DefaultMutableTreeNode::getUserObject)
                .filter(expectedClass::isInstance).map(expectedClass::cast);
    }

    @Nullable
    public DefaultMutableTreeNode getModelRoot() {
        return (DefaultMutableTreeNode) getModel().getRoot();
    }

    public void setJobs(@NotNull final Collection<Job> jobs) {
        Optional.ofNullable(getModelRoot()).ifPresent(rootNode -> setJobs(jobs, rootNode));
    }

    private void setJobs(@NotNull final Collection<Job> jobs, @NotNull DefaultMutableTreeNode rootNode) {
        rootNode.removeAllChildren();
        jobs.stream().map(JenkinsTree::createJobTree).forEach(rootNode::add);
        tree.setRootVisible(true);
    }

    public void keepLastState(@NotNull final Runnable runnable) {
        final Optional<TreeState> treeState = getLastTreeState();
        runnable.run();
        treeState.ifPresent(t -> t.applyTo(tree, getModelRoot()));
        saveLastTreeState();
    }

    private void saveLastTreeState() {
        this.lastTreeState = getTreeState().orElse(NO_TREE_STATE);
    }

    public void setJobsUnavailable() {
        tree.setRootVisible(false);
        tree.getEmptyText().setText(UNAVAILABLE);
    }

    public void updateSelection() {
        Optional.ofNullable(tree.getSelectionPath()).map(TreePath::getLastPathComponent)
                .map(TreeNode.class::cast)
                .ifPresent(node -> getModel().nodeChanged(node));
    }

    @Nullable
    @Override
    public JenkinsTreeState getState() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        getTreeState().ifPresent(treeState -> {
            try {
                state.treeState = new Element("root");
                treeState.writeExternal(state.treeState);
            } catch (WriteExternalException e) {
                LOG.warn(e);
            }
        });
        return state;
    }

    @NotNull
    public Optional<TreeState> getTreeState() {
        return Optional.ofNullable(getModelRoot()).map(modelRoot -> TreeState.createOn(tree, modelRoot));
    }

    @NotNull
    private Optional<TreeState> getLastTreeState() {
        final Integer childCount = Optional.ofNullable(getModelRoot()).map(DefaultMutableTreeNode::getChildCount).orElse(0);
        return childCount == 0 ? Optional.ofNullable(lastTreeState) : getTreeState();
    }

    @Override
    public void loadState(@NotNull JenkinsTreeState state) {
        this.state = state;
        this.lastTreeState = TreeState.createFrom(state.treeState);
    }

    public void updateJobNode(@NotNull Job job) {
        final DefaultTreeModel model = getModel();
        findNodes(job).forEach(jobNode -> {
            fillJobTree(job, jobNode);
            model.nodeChanged(jobNode);
            model.nodeStructureChanged(jobNode);
        });
    }

    @NotNull
    private Collection<DefaultMutableTreeNode> findNodes(@NotNull Job job) {
        final Enumeration<TreeNode> allNodes = Optional.ofNullable(getModelRoot())
                .map(DefaultMutableTreeNode::depthFirstEnumeration).orElse(Collections.emptyEnumeration());

        final List<DefaultMutableTreeNode> jobNodes = new ArrayList<>();
        while (allNodes.hasMoreElements()) {
            final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) allNodes.nextElement();
            getJob(childNode)
                    .filter(childJob -> isSameJob(job, childJob))
                    .map(childJob -> childNode)
                    .ifPresent(jobNodes::add);
        }
        return jobNodes;
    }

    @NotNull
    private DefaultTreeModel getModel() {
        return (DefaultTreeModel) tree.getModel();
    }

    private boolean isSameJob(Job job1, Job job2) {
        return job1.getUrl().equals(job2.getUrl());
    }

    public void sortJobs(Comparator<Job> comparator) {
        final DefaultTreeModel model = getModel();
        TreeUtil.sort(model, wrapJobSorter(comparator));
        GuiUtil.runInSwingThread(() -> model.nodeStructureChanged((TreeNode) model.getRoot()));
    }

    public void updateDoubleClickAction(@NotNull JobAction doubleClickAction) {
        Optional.ofNullable(clickHandler).ifPresent(this.tree::removeMouseListener);
        this.clickHandler = new JobClickHandler(doubleClickAction);
        this.tree.addMouseListener(clickHandler);
    }

    @SuppressWarnings("java:S110")
    private static class TreeWithoutDefaultSearch extends SimpleTree {

        @Override
        protected void configureUiHelper(TreeUIHelper helper) {
            final Convertor<TreePath, String> convertor = treePath -> JenkinsTree.getJob(treePath).map(Job::preferDisplayName).orElse("");
            helper.installTreeSpeedSearch(this, convertor, true);
        }
    }
}
