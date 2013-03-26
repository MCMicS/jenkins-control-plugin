/*
 * Copyright (c) 2013 David Boissier
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
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.JenkinsWindowManager;
import org.codinjutsu.tools.jenkins.logic.BuildStatusAggregator;
import org.codinjutsu.tools.jenkins.logic.BuildStatusVisitor;
import org.codinjutsu.tools.jenkins.logic.JenkinsLoadingTaskOption;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.action.*;
import org.codinjutsu.tools.jenkins.view.action.settings.SortByStatusAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BrowserPanel extends SimpleToolWindowPanel implements Disposable {

    private static final URL pluginSettingsUrl = GuiUtil.isUnderDarcula() ? GuiUtil.getIconResource("settings_dark.png") : GuiUtil.getIconResource("settings.png");

    private static final String UNAVAILABLE = String.format("<html><center>No Jenkins server available<br><br>You may use <img src=\"%s\"> to add or fix configuration</center></html>", pluginSettingsUrl);
    private static final String LOADING = "Loading...";

    private JPanel rootPanel;

    private JPanel jobPanel;
    private Tree jobTree;

    private final JobComparator jobStatusComparator = new JobStatusComparator();
    private boolean sortedByBuildStatus;

    private final Runnable refreshViewJob;
    private ScheduledFuture<?> refreshViewFutureTask;

    private final Project project;

    private final JenkinsAppSettings jenkinsAppSettings;
    private final JenkinsSettings jenkinsSettings;

    private final RequestManager requestManager;

    private final Jenkins jenkins;
    private FavoriteView favoriteView;
    private View currentSelectedView;


    public static BrowserPanel getInstance(Project project) {
        return ServiceManager.getService(project, BrowserPanel.class);
    }

    public BrowserPanel(final Project project) {

        super(true);
        this.project = project;

        refreshViewJob = new Runnable() {
            @Override
            public void run() {
                GuiUtil.runInSwingThread(new Runnable() {
                    @Override
                    public void run() {
                        new LoadSelectedViewJob(project).queue();
                    }
                });
            }
        };

        requestManager = RequestManager.getInstance(project);
        jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        jenkinsSettings = JenkinsSettings.getSafeInstance(project);
        setProvideQuickActions(false);

        jenkins = Jenkins.byDefault();
        jobTree = createTree(jenkinsSettings.getFavoriteJobs());


        jobPanel.setLayout(new BorderLayout());
        jobPanel.add(ScrollPaneFactory.createScrollPane(jobTree), BorderLayout.CENTER);

        setContent(rootPanel);
    }


    public void fillData(Jenkins jenkins) {
        this.jenkins.update(jenkins);
        fillJobTree(BuildStatusVisitor.NULL);
    }

    public void initScheduledJobs(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        safeTaskCancel(refreshViewFutureTask);
        scheduledThreadPoolExecutor.remove(refreshViewJob);

        if (jenkinsAppSettings.getJobRefreshPeriod() > 0) {
            refreshViewFutureTask = scheduledThreadPoolExecutor.scheduleWithFixedDelay(refreshViewJob, jenkinsAppSettings.getJobRefreshPeriod(), jenkinsAppSettings.getJobRefreshPeriod(), TimeUnit.MINUTES);
        }
    }

    private void safeTaskCancel(ScheduledFuture<?> futureTask) {
        if (futureTask == null) {
            return;
        }
        if (!futureTask.isDone() || !futureTask.isCancelled()) {
            futureTask.cancel(false);
        }
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

    public List<Job> getAllSelectedJobs() {
        return TreeUtil.collectSelectedObjectsOfType(jobTree, Job.class);
    }

    public void fillJobTree(BuildStatusVisitor buildStatusVisitor) {
        List<Job> jobList = jenkins.getJobs();
        if (jobList.isEmpty()) {
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
        jobTree.setRootVisible(true);
        jobTree.setModel(treeModel);
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

    public JTree getJobTree() {
        return jobTree;
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

    public void update() {
        ((DefaultTreeModel) jobTree.getModel()).nodeChanged((TreeNode) jobTree.getSelectionPath().getLastPathComponent());
    }

    public Jenkins getJenkins() {
        return jenkins;
    }

    public View getCurrentSelectedView() {
        return currentSelectedView;
    }

    public RequestManager getJenkinsManager() {
        return requestManager;
    }

    public void loadSelectedJob() {
        Job job = getSelectedJob();
        Job updatedJob = requestManager.loadJob(job.getUrl());
        job.updateContentWith(updatedJob);
    }

    public boolean hasFavoriteJobs() {
        return !jenkinsSettings.getFavoriteJobs().isEmpty();
    }

    public void notifyInfoJenkinsToolWindow(String htmlLinkMessage) {
        ToolWindowManager.getInstance(project).notifyByBalloon(
                JenkinsWindowManager.JENKINS_BROWSER,
                MessageType.INFO,
                htmlLinkMessage,
                null,
                new BrowserHyperlinkListener());
    }

    public void notifyErrorJenkinsToolWindow(String message) {
        ToolWindowManager.getInstance(project).notifyByBalloon(JenkinsWindowManager.JENKINS_BROWSER, MessageType.ERROR, message);
    }

    private class JobStatusComparator implements JobComparator {
        @Override
        public int compare(DefaultMutableTreeNode treeNode1, DefaultMutableTreeNode treeNode2) {
            Job job1 = ((Job) treeNode1.getUserObject());
            Job job2 = ((Job) treeNode2.getUserObject());

            return new Integer(BuildStatusEnum.getStatus(job1.getColor()).ordinal()).compareTo(BuildStatusEnum.getStatus(job2.getColor()).ordinal());
        }


        public boolean isApplicable() {
            return sortedByBuildStatus;
        }
    }


    private Tree createTree(List<JenkinsSettings.FavoriteJob> favoriteJobs) {


        SimpleTree tree = new SimpleTree() {

            private final JLabel myLabel = new JLabel(LOADING);

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (jenkinsAppSettings.isServerUrlSet() && !jenkins.getJobs().isEmpty()) return;

                if (!jenkinsAppSettings.isServerUrlSet()) {
                    myLabel.setText(UNAVAILABLE);
                } else {
                    myLabel.setText(LOADING);
                }

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
        tree.setCellRenderer(new JenkinsTreeRenderer(favoriteJobs));
        tree.setName("jobTree");

        tree.setRootVisible(false);

        new TreeSpeedSearch(tree, new Convertor<TreePath, String>() {

            @Override
            public String convert(TreePath treePath) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                final Object userObject = node.getUserObject();
                if (userObject instanceof Job) {
                    return ((Job) userObject).getName();
                }
                return "<empty>";
            }
        });

        return tree;
    }

    public void reloadConfiguration() {
        if (!jenkinsAppSettings.isServerUrlSet()) {
            JenkinsWidget.getInstance(project).updateStatusIcon(new BuildStatusAggregator(0));//TODO Crappy, need rewrite this
            return;
        }

        try {
            requestManager.authenticate(jenkinsAppSettings, jenkinsSettings);
        } catch (Exception ex) {
            displayConnectionErrorMsg();
            return;
        }

        loadJenkinsWorkspace();

        if (!jenkinsSettings.getFavoriteJobs().isEmpty()) {
            createFavoriteViewIfNecessary();
        }

        String lastSelectedViewName = jenkinsSettings.getLastSelectedView();
        View viewToLoad;
        if (StringUtils.isNotEmpty(lastSelectedViewName)) {
            viewToLoad = jenkins.getViewByName(lastSelectedViewName);
        } else {
            viewToLoad = jenkins.getPrimaryView();
        }
        loadView(viewToLoad);
    }

    public void init() {
        initGui();
        reloadConfiguration();
    }

    private void initGui() {
        installBrowserActions(getJobTree());
    }

    protected void installBrowserActions(JTree jobTree) {
        DefaultActionGroup actionGroup = new DefaultActionGroup("JenkinsToolbarGroup", false);
        actionGroup.add(new SelectViewAction(this));
        actionGroup.add(new RefreshNodeAction(this));
        actionGroup.add(new RunBuildAction(this));
        actionGroup.add(new SortByStatusAction(this));
        actionGroup.add(new RefreshRssAction());
        actionGroup.addSeparator();
        actionGroup.add(new OpenPluginSettingsAction());

        GuiUtil.installActionGroupInToolBar(actionGroup, this, ActionManager.getInstance(), "jenkinsBrowserActions");

        DefaultActionGroup popupGroup = new DefaultActionGroup("JenkinsPopupAction", true);
        popupGroup.add(new SetJobAsFavoriteAction(this));
        popupGroup.add(new UnsetJobAsFavoriteAction(this));
        popupGroup.addSeparator();
        popupGroup.add(new GotoJobPageAction(this));
        popupGroup.add(new GotoLastBuildPageAction(this));

        installActionGroupInPopupMenu(popupGroup, jobTree, ActionManager.getInstance());
    }

    private static void installActionGroupInPopupMenu(ActionGroup group,
                                                      JComponent component,
                                                      ActionManager actionManager) {
        if (actionManager == null) {
            return;
        }
        PopupHandler.installPopupHandler(component, group, "POPUP", actionManager);
    }

    private void displayConnectionErrorMsg() {
        setErrorMsg(jenkinsAppSettings.getServerUrl(), "(Unable to connect. Check Jenkins Plugin Settings.)");
    }

    private void loadJenkinsWorkspace() {
        Jenkins jenkins = requestManager.loadJenkinsWorkspace(jenkinsAppSettings);
        fillData(jenkins);
    }

    public void loadView(View view) {
        if (view != null) {//TODO to be removed
            this.currentSelectedView = view;
        }
        new LoadSelectedViewJob(project).queue();
    }

    public void setAsFavorite(List<Job> jobs) {
        jenkinsSettings.addFavorite(jobs);
        createFavoriteViewIfNecessary();
        update();

    }

    private void createFavoriteViewIfNecessary() {
        if (favoriteView == null) {
            favoriteView = FavoriteView.create();
        }
    }

    public void removeFavorite(List<Job> selectedJobs) {
        jenkinsSettings.removeFavorite(selectedJobs);
        update();
        if (jenkinsSettings.isFavoriteViewEmpty() && currentSelectedView instanceof FavoriteView) {
            favoriteView = null;
            loadView(jenkins.getPrimaryView());
        } else {
            if (currentSelectedView instanceof FavoriteView) {
                loadView(currentSelectedView);
            }
        }
    }

    public boolean isAFavoriteJob(String jobName) {
        return jenkinsSettings.isAFavoriteJob(jobName);
    }

    private class LoadSelectedViewJob extends Task.Backgroundable {
        public LoadSelectedViewJob(@Nullable Project project) {
            super(project, "Loading Jenkins Jobs", true, JenkinsLoadingTaskOption.INSTANCE);
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {

            if (currentSelectedView == null) {
                if (jenkins == null) {
                    return;
                }
                currentSelectedView = jenkins.getPrimaryView();
            }

            GuiUtil.runInSwingThread(new Runnable() {
                @Override
                public void run() {
                    jobTree.setPaintBusy(true);
                }
            });

            final List<Job> jobList;
            if (currentSelectedView instanceof FavoriteView) {
                jobList = requestManager.loadFavoriteJobs(jenkinsSettings.getFavoriteJobs());
            } else {
                jobList = requestManager.loadJenkinsView(currentSelectedView.getUrl());
            }

            jenkinsSettings.setLastSelectedView(currentSelectedView.getName());

            jenkins.setJobs(jobList);
            final BuildStatusAggregator buildStatusAggregator = new BuildStatusAggregator(jobList.size());

            GuiUtil.runInSwingThread(new Runnable() {
                @Override
                public void run() {


                    fillJobTree(buildStatusAggregator);
                    jobTree.setPaintBusy(false);
                    JenkinsWidget.getInstance(project).updateStatusIcon(buildStatusAggregator);
                }
            });
        }
    }
}