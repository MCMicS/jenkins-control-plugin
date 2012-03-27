/*
 * Copyright (c) 2011 David Boissier
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

import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class JenkinsBrowserPanel extends JPanel {
    private JComboBox viewCombo;
    private JTree jobTree;
    private JPanel rootPanel;
    private JPanel actionPanel;
    private JPanel utilityPanel;
    private JobSearchComponent searchComponent;


    public JenkinsBrowserPanel() {
        jobTree.setCellRenderer(new JenkinsTreeRenderer());
        jobTree.setName("jobTree");
        viewCombo.setName("viewCombo");

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
    }


    public void initModel(Jenkins jenkins) {
        fillJobTree(jenkins);
        initViewList(jenkins.getViews());
    }

    public void createSearchPanel() {
        searchComponent = new JobSearchComponent(jobTree);
        utilityPanel.add(searchComponent, BorderLayout.CENTER);
    }


    public void fillJobTree(Jenkins jenkins) {
        List<Job> jobList = jenkins.getJobList();
        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(jenkins);
        if (!jobList.isEmpty()) {
            for (Job job : jobList) {
                DefaultMutableTreeNode jobNode = new DefaultMutableTreeNode(job);
                rootNode.add(jobNode);
            }
        }
        GuiUtil.runInSwingThread(new Runnable() {
            public void run() {
                jobTree.setModel(new DefaultTreeModel(rootNode));
            }
        });
    }


    private void initViewList(List<View> views) {
        List<View> flattenViewList = flatViewList(views);

        viewCombo.setModel(new JenkinsViewComboboxModel(flattenViewList));
        if (hasNestedViews(views)) {
            viewCombo.setRenderer(new JenkinsNestedViewComboRenderer());
        } else {
            viewCombo.setRenderer(new JenkinsViewComboRenderer());
        }
        viewCombo.setSelectedIndex(-1);
    }

    private List<View> flatViewList(List<View> views) {
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


    public void setSelectedView(View view) {
        viewCombo.setSelectedItem(view);
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


    public void showErrorDialog(String errorMessage, String content) {
        GuiUtil.showErrorDialog(errorMessage, content);
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


    public JTree getJobTree() {
        return jobTree;
    }


    public JComboBox getViewCombo() {
        return viewCombo;
    }


    public void setErrorMsg() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new Jenkins(
                "(Unable to connect. Check Jenkins Plugin Settings.)"));
        jobTree.setModel(new DefaultTreeModel(rootNode));
    }

    public JPanel getActionPanel() {
        return actionPanel;
    }

    public JobSearchComponent getSearchComponent() {
        return searchComponent;
    }
}
