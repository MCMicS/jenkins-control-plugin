package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.project.Project;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.containers.Convertor;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BuildStatusEnumRenderer;
import org.codinjutsu.tools.jenkins.view.JenkinsTreeRenderer;
import org.codinjutsu.tools.jenkins.view.JobClickHandler;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class JenkinsTree extends SimpleTree {
    private static final String LOADING = "Loading...";
    private final transient Project project;
    @NotNull
    private final JenkinsSettings jenkinsSettings;
    private final Jenkins jenkins;

    public JenkinsTree(Project project, @NotNull JenkinsSettings jenkinsSettings, Jenkins jenkins) {
        super();
        this.project = project;
        this.jenkinsSettings = jenkinsSettings;
        this.jenkins = jenkins;
        getEmptyText().setText(LOADING);
        setCellRenderer(new JenkinsTreeRenderer(this.jenkinsSettings::isFavoriteJob,
                BuildStatusEnumRenderer.getInstance(this.project)));
        setName("jobTree");
        setModel(new DefaultTreeModel(new DefaultMutableTreeNode(this.jenkins), false));
        //final JobTreeHandler jobTreeHandler = new JobTreeHandler(project);
        //addTreeWillExpandListener(jobTreeHandler);
        addMouseListener(new JobClickHandler());
    }

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
