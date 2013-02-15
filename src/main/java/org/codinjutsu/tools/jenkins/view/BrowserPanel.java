/*
 * Copyright (c) 2012 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.view;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.Tree;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.logic.BuildStatusVisitor;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BrowserPanel extends SimpleToolWindowPanel implements Disposable {

    private static final URL pluginSettingsUrl = GuiUtil.isUnderDarcula() ? GuiUtil.getIconResource("settings_dark.png") : GuiUtil.getIconResource("settings.png");

    private JPanel rootPanel;
    private JPanel actionPanel;
    private JPanel utilityPanel;
    private JPanel jobPanel;

    private JComboBox viewCombo;
    private JobSearchComponent searchComponent;
    private Tree jobTree;

    private boolean sortedByBuildStatus;
    private final JobComparator jobStatusComparator = new JobStatusComparator();
    private Jenkins jenkins;


    public BrowserPanel(List<JenkinsSettings.FavoriteJob> favoriteJobs) {
        super(true);
        setProvideQuickActions(false);

        viewCombo.setName("viewCombo");

        jobTree = createTree(favoriteJobs);
        jobPanel.setLayout(new BorderLayout());
        jobPanel.add(new JBScrollPane(jobTree), BorderLayout.CENTER);

        setContent(rootPanel);
    }


    public void fillData(Jenkins jenkins) {
        this.jenkins = jenkins;
        fillViewCombo(jenkins);
        fillJobTree(jenkins, BuildStatusVisitor.NULL);
    }


    public void createSearchPanel() {
        searchComponent = new JobSearchComponent(jobTree);
        utilityPanel.add(searchComponent, BorderLayout.CENTER);
    }


    public Job getSelectedJob() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jobTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof Job) {
                return (Job) userObject;
            }
        }
        return null;
    }


    public View getSelectedJenkinsView() {
        return (View) viewCombo.getSelectedItem();
    }


    public void selectView(String name) {
        for (int i = 0; i < viewCombo.getItemCount(); i++) {
            View view = (View) viewCombo.getItemAt(i);
            if (StringUtils.equals(name, view.getName())) {
                viewCombo.setSelectedItem(view);
                return;
            }
        }
    }


    public Jenkins getJenkins() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jobTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof Jenkins) {
                return (Jenkins) userObject;
            }
        }
        return null;
    }


    public void fillJobTree(Jenkins jenkins, BuildStatusVisitor buildStatusVisitor) {
        List<Job> jobList = jenkins.getJobList();
        if (jenkins.getJobList().isEmpty()) {
            jobTree.setRootVisible(false);
            return;
        }

        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(jenkins);

        for (Job job : jobList) {
            DefaultMutableTreeNode jobNode = new DefaultMutableTreeNode(job);
            rootNode.add(jobNode);
            visit(job, buildStatusVisitor);
        }

        JenkinsTreeModel treeModel = new JenkinsTreeModel(rootNode);
        treeModel.setJobStatusComparator(jobStatusComparator);
        jobTree.setModel(treeModel);
        jobTree.setRootVisible(true);

    }


    public void setErrorMsg(String serverUrl, String description) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new Jenkins(description, serverUrl));
        jobTree.setModel(new DefaultTreeModel(rootNode));
    }

    public void setSortedByStatus(boolean selected) {
        sortedByBuildStatus = selected;
        ((DefaultTreeModel) jobTree.getModel()).reload();
        jobTree.repaint();

    }


    public void updateViewCombo(final FavoriteView favoriteView) {
        GuiUtil.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                viewCombo.invalidate();
                DefaultComboBoxModel model = (DefaultComboBoxModel) viewCombo.getModel();
                model.addElement(favoriteView);
                viewCombo.repaint();
                viewCombo.revalidate();
            }
        });
    }


    public void resetViewCombo(final List<View> views) {
        GuiUtil.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                viewCombo.invalidate();
                DefaultComboBoxModel model = (DefaultComboBoxModel) viewCombo.getModel();
                model.removeAllElements();
                for (View view : views) {
                    model.addElement(view);
                }
                viewCombo.repaint();
                viewCombo.revalidate();
            }
        });
    }


    public FavoriteView getFavoriteView() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) viewCombo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            View view = (View) model.getElementAt(i);
            if (view instanceof FavoriteView) {
                return (FavoriteView) view;
            }
        }
        return null;
    }


    public JTree getJobTree() {
        return jobTree;
    }


    public JComboBox getViewCombo() {
        return viewCombo;
    }


    public JPanel getActionPanel() {
        return actionPanel;
    }


    public JobSearchComponent getSearchComponent() {
        return searchComponent;
    }


    public boolean isSortedByStatus() {
        return sortedByBuildStatus;
    }


    @Override
    public void dispose() {

    }


    private static void visit(Job job, BuildStatusVisitor buildStatusVisitor) {
        Build lastBuild = job.getLastBuild();
        if (job.isBuildable() && lastBuild != null) {
            BuildStatusEnum status = lastBuild.getStatus();
            if (BuildStatusEnum.FAILURE == status) {
                buildStatusVisitor.visitFailed();
                return;
            }
            if (BuildStatusEnum.SUCCESS == status) {
                buildStatusVisitor.visitSuccess();
                return;
            }
            if (BuildStatusEnum.UNSTABLE == status) {
                buildStatusVisitor.visitUnstable();
                return;
            }
            if (BuildStatusEnum.ABORTED == status) {
                buildStatusVisitor.visitAborted();
                return;
            }
            if (BuildStatusEnum.NULL == status) {
                buildStatusVisitor.visitUnknown();
                return;
            }
        }

        buildStatusVisitor.visitUnknown();
    }


    private void fillViewCombo(final Jenkins jenkins) {
        GuiUtil.runInSwingThread(new Runnable() {
            public void run() {
                List<View> views = jenkins.getViews();
                viewCombo.setModel(new JenkinsViewComboboxModel(flatViewList(views)));
                if (hasNestedViews(views)) {
                    viewCombo.setRenderer(new JenkinsNestedViewComboRenderer());
                } else {
                    viewCombo.setRenderer(new JenkinsViewComboRenderer());
                }
            }
        });
    }


    private static List<View> flatViewList(List<View> views) {
        List<View> flattenViewList = new LinkedList<View>();
        for (View view : views) {
            flattenViewList.add(view);
            if (view.hasNestedView()) {
                for (View subView : view.getSubViews()) {
                    flattenViewList.add(subView);
                }
            }
        }

        return flattenViewList;
    }


    private static boolean hasNestedViews(List<View> views) {
        for (View view : views) {
            if (view.hasNestedView()) return true;
        }
        return false;
    }

    public void update() {
        ((DefaultTreeModel) jobTree.getModel()).nodeChanged((TreeNode) jobTree.getSelectionPath().getLastPathComponent());
    }


    private class JobStatusComparator implements JobComparator {
        @Override
        public int compare(DefaultMutableTreeNode treeNode1, DefaultMutableTreeNode treeNode2) {
            Job job1 = ((Job) treeNode1.getUserObject());
            Job job2 = ((Job) treeNode2.getUserObject());

            return new Integer(getStatus(job1.getColor()).ordinal()).compareTo(getStatus(job2.getColor()).ordinal());
        }


        public boolean isApplicable() {
            return sortedByBuildStatus;
        }
    }


    private static BuildStatusEnum getStatus(String jobColor) {
        BuildStatusEnum[] jobStates = BuildStatusEnum.values();
        for (BuildStatusEnum jobStatus : jobStates) {
            String stateName = jobStatus.getColor();
            if (jobColor.startsWith(stateName)) {
                return jobStatus;
            }
        }

        return BuildStatusEnum.NULL;
    }

    private Tree createTree(List<JenkinsSettings.FavoriteJob> favoriteJobs) {

        SimpleTree tree = new SimpleTree() {

            private final JLabel myLabel = new JLabel(
                    String.format("<html><center>No Jenkins server available<br><br>You may use <img src=\"%s\"> to add or fix configuration</center></html>", pluginSettingsUrl)
            );

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (jenkins != null && !jenkins.getJobList().isEmpty()) return;

                myLabel.setFont(getFont());
                myLabel.setBackground(getBackground());
                myLabel.setForeground(getForeground());
                Rectangle bounds = getBounds();
                Dimension size = myLabel.getPreferredSize();
                myLabel.setBounds(0, 0, size.width, size.height);

                int x = (bounds.width - size.width) / 2;
                Graphics g2 = g.create(bounds.x + x, bounds.y + 20, bounds.width, bounds.height);
                try {
                    myLabel.paint(g2);
                } finally {
                    g2.dispose();
                }
            }
        };

        tree.getEmptyText().clear();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.setCellRenderer(new JenkinsTreeRenderer(favoriteJobs));
        tree.setName("jobTree");

        tree.setRootVisible(false);

        return tree;
    }
}
