package org.codinjutsu.tools.jenkins;

import com.intellij.ide.util.treeView.TreeBuilderUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.containers.Convertor;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BuildStatusEnumRenderer;
import org.codinjutsu.tools.jenkins.view.JenkinsTreeRenderer;
import org.codinjutsu.tools.jenkins.view.JobClickHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Optional;

public class JenkinsTree {
    private static final String LOADING = "Loading...";
    private static final String UNAVAILABLE = "No Jenkins server available";
    @NotNull
    private final JenkinsSettings jenkinsSettings;
    private final Jenkins jenkins;
    private final SimpleTree tree;

    public JenkinsTree(Project project, @NotNull JenkinsSettings jenkinsSettings, Jenkins jenkins) {
        super();
        this.jenkinsSettings = jenkinsSettings;
        this.jenkins = jenkins;
        this.tree = new TreeWithoutDefaultSearch();


        this.tree.getEmptyText().setText(LOADING);
        this.tree.setCellRenderer(new JenkinsTreeRenderer(this.jenkinsSettings::isFavoriteJob,
                BuildStatusEnumRenderer.getInstance(project)));
        this.tree.setName("jobTree");
        this.tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(this.jenkins), false));
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
    private static DefaultMutableTreeNode createNode(Build build) {
        return new DefaultMutableTreeNode(build, false);
    }

    @NotNull
    private static DefaultMutableTreeNode createNode(Job job) {
        boolean allowsChildren = true;
        return new DefaultMutableTreeNode(job, allowsChildren);
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
        return (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    }

    @Nullable
    public <T> T getLastSelectedPath(@NotNull Class<T> expectedClass) {
        return Optional.ofNullable(getLastSelectedPathComponent())
                .map(DefaultMutableTreeNode::getUserObject)
                .filter(expectedClass::isInstance).map(expectedClass::cast)
                .orElse(null);
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
        final TreeModel model = tree.getModel();
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        rootNode.removeAllChildren();
        jobs.stream().map(JenkinsTree::createJobTree).forEach(rootNode::add);
        tree.setRootVisible(true);
    }

    public void setJobsUnavailable() {
        tree.setRootVisible(false);
        tree.getEmptyText().setText(UNAVAILABLE);
    }

    @NotNull
    public Optional<DefaultMutableTreeNode> findNode(@NotNull Job job) {
        final DefaultMutableTreeNode modelRoot = getModelRoot();
        final Enumeration<TreeNode> allNodes = modelRoot.depthFirstEnumeration();

        DefaultMutableTreeNode jobNode = null;
        while (allNodes.hasMoreElements() && jobNode == null) {
            final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) allNodes.nextElement();
            // TODO mcmics: use equals
            if (childNode.getUserObject() == job) {
                jobNode = childNode;
            }
        }
        return Optional.ofNullable(jobNode);
    }

    public void updateSelection() {
        Optional.ofNullable(tree.getSelectionPath()).map(TreePath::getLastPathComponent)
                .map(TreeNode.class::cast)
                .ifPresent(node -> getModel().nodeChanged(node));
    }

    @SuppressWarnings("java:S110")
    private static class TreeWithoutDefaultSearch extends SimpleTree {

        @Override
        protected void configureUiHelper(TreeUIHelper helper) {
            final Convertor<TreePath, String> convertor = treePath -> {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                final Object userObject = node.getUserObject();
                if (userObject instanceof Job) {
                    //return ((Job) userObject).getNameToRenderSingleJob();
                    return ((Job) userObject).preferDisplayName();
                }
                return "";
            };
            helper.installTreeSpeedSearch(this, convertor, true);
        }
    }
}
