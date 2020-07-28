package org.codinjutsu.tools.jenkins;

import com.intellij.ide.util.treeView.TreeState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.containers.Convertor;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BuildStatusEnumRenderer;
import org.codinjutsu.tools.jenkins.view.JenkinsTreeNode;
import org.codinjutsu.tools.jenkins.view.JenkinsTreeRenderer;
import org.codinjutsu.tools.jenkins.view.JobClickHandler;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

public class JenkinsTree implements PersistentStateComponent<JenkinsTreeState> {
    private static final String LOADING = "Loading...";
    private static final String UNAVAILABLE = "No Jenkins server available";
    @NotNull
    private final JenkinsSettings jenkinsSettings;
    private final Jenkins jenkins;
    private final SimpleTree tree;
    private JenkinsTreeState state = new JenkinsTreeState();
    private static final Logger LOG = Logger.getInstance(JenkinsTree.class);

    public JenkinsTree(Project project, @NotNull JenkinsSettings jenkinsSettings, Jenkins jenkins) {
        super();
        this.jenkinsSettings = jenkinsSettings;
        this.jenkins = jenkins;
        this.tree = new TreeWithoutDefaultSearch();

        this.tree.getEmptyText().setText(LOADING);
        this.tree.setCellRenderer(new JenkinsTreeRenderer(this.jenkinsSettings::isFavoriteJob,
                BuildStatusEnumRenderer.getInstance(project)));
        this.tree.setName("jobTree");
        this.tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(new JenkinsTreeNode.RootNode(this.jenkins)), false));
        //final JobTreeHandler jobTreeHandler = new JobTreeHandler(project);
        //addTreeWillExpandListener(jobTreeHandler);
        this.tree.addMouseListener(new JobClickHandler());
    }

    @NotNull
    public static DefaultMutableTreeNode fillJobTree(@NotNull Job job, @NotNull DefaultMutableTreeNode jobNode) {
        jobNode.removeAllChildren();
        if (job.getJobType().containNestedJobs()) {
            job.getNestedJobs().stream().map(JenkinsTree::createJobTree).forEach(jobNode::add);
        } else {
            job.getLastBuilds().stream().map(JenkinsTree::createNode).forEach(jobNode::add);
        }
        return jobNode;
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
    private static DefaultMutableTreeNode createNode(Build build) {
        return new DefaultMutableTreeNode(new JenkinsTreeNode.BuildNode(build), false);
    }

    @NotNull
    private static DefaultMutableTreeNode createNode(Job job) {
        boolean allowsChildren = true;
        return new DefaultMutableTreeNode(new JenkinsTreeNode.JobNode(job), allowsChildren);
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

    @NotNull
    public <T> Optional<T> getLastSelectedPath(@NotNull Class<T> expectedClass) {
        return Optional.ofNullable(getLastSelectedPathComponent())
                .map(DefaultMutableTreeNode::getUserObject)
                .filter(expectedClass::isInstance).map(expectedClass::cast);
    }

    @NotNull
    public DefaultTreeModel getModel() {
        return (DefaultTreeModel) tree.getModel();
    }

    @Nullable
    public DefaultMutableTreeNode getModelRoot() {
        return (DefaultMutableTreeNode) getModel().getRoot();
    }

    public void setJobs(@NotNull final Collection<Job> jobs) {
        final Optional<TreeState> treeState = getTreeState();
        final TreeModel model = tree.getModel();
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        rootNode.removeAllChildren();
        jobs.stream().map(JenkinsTree::createJobTree).forEach(rootNode::add);
        tree.setRootVisible(true);
        treeState.ifPresent(t -> t.applyTo(tree, rootNode));
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
            }
            catch (WriteExternalException e) {
                LOG.warn(e);
            }
        });
        return state;
    }

    @NotNull
    private Optional<TreeState> getTreeState() {
        return Optional.ofNullable(getModelRoot()).map(modelRoot -> TreeState.createOn(tree, modelRoot));
    }

    @Override
    public void loadState(@NotNull JenkinsTreeState state) {
        this.state = state;
    }

    public void updateJobNode(@NotNull Job job) {
        final DefaultTreeModel model = getModel();
        findNodes(job).forEach(jobNode -> {
            JenkinsTree.fillJobTree(job, jobNode);
            model.nodeChanged(jobNode);
            model.nodeStructureChanged(jobNode);
        });
    }

    @NotNull
    private Collection<DefaultMutableTreeNode> findNodes(@NotNull Job job) {
        final DefaultMutableTreeNode modelRoot = getModelRoot();
        final Enumeration<TreeNode> allNodes = modelRoot.depthFirstEnumeration();

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

    private boolean isSameJob(Job job1, Job job2) {
        return job1.getUrl().equals(job2.getUrl());
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
