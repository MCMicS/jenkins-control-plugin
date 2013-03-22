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
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.logic.BuildStatusAggregator;
import org.codinjutsu.tools.jenkins.logic.BuildStatusVisitor;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.action.*;
import org.codinjutsu.tools.jenkins.view.action.search.NextOccurrenceAction;
import org.codinjutsu.tools.jenkins.view.action.search.OpenJobSearchPanelAction;
import org.codinjutsu.tools.jenkins.view.action.search.PrevOccurrenceAction;
import org.codinjutsu.tools.jenkins.view.action.settings.SortByStatusAction;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BrowserPanel extends SimpleToolWindowPanel implements Disposable {

    private static final URL pluginSettingsUrl = GuiUtil.isUnderDarcula() ? GuiUtil.getIconResource("settings_dark.png") : GuiUtil.getIconResource("settings.png");

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);

    private final Project project;

    private JPanel rootPanel;
    private JPanel utilityPanel;
    private JPanel jobPanel;

    private JobSearchComponent searchComponent;
    private Tree jobTree;

    private boolean sortedByBuildStatus;
    private final JobComparator jobStatusComparator = new JobStatusComparator();

    private Jenkins jenkins;

    private JenkinsAppSettings jenkinsAppSettings;
    private JenkinsSettings jenkinsSettings;
    private RequestManager requestManager;

    private FavoriteView favoriteView;
    private View currentSelectedView;

    private ScheduledFuture<?> refreshViewFutureTask;
    private final Runnable refreshViewJob = new LoadSelectedViewJob();


    public static BrowserPanel getInstance(Project project) {
        return ServiceManager.getService(project, BrowserPanel.class);
    }

    public BrowserPanel(Project project) {

        super(true);
        this.project = project;
        requestManager = RequestManager.getInstance(project);
        jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        jenkinsSettings = JenkinsSettings.getSafeInstance(project);
        setProvideQuickActions(false);

        jobTree = createTree(jenkinsSettings.getFavoriteJobs());
        jobPanel.setLayout(new BorderLayout());
        jobPanel.add(new JBScrollPane(jobTree), BorderLayout.CENTER);

        setContent(rootPanel);
    }


    public void fillData(Jenkins jenkins) {
        this.jenkins = jenkins;
        fillJobTree(BuildStatusVisitor.NULL);
    }

    public void createSearchPanel() {
        searchComponent = new JobSearchComponent(jobTree);
        utilityPanel.add(searchComponent, BorderLayout.CENTER);
    }

    public void initScheduledJobs() {
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

    public JobSearchComponent getSearchComponent() {
        return searchComponent;
    }

    @Override
    public void dispose() {
        scheduledThreadPoolExecutor.shutdown();
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
//        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.setCellRenderer(new JenkinsTreeRenderer(favoriteJobs));
        tree.setName("jobTree");

        tree.setRootVisible(false);

        return tree;
    }

    public void reloadConfiguration() {
        if (!jenkinsAppSettings.isServerUrlSet()) {
            JenkinsWidget.getInstance(project).updateStatusIcon(new BuildStatusAggregator());//TODO Crappy, need rewrite this
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

    public void init(RefreshRssAction refreshRssAction) {
        initGui(refreshRssAction);
        reloadConfiguration();
    }

    private void initGui(RefreshRssAction refreshRssAction) {
        createSearchPanel();
        installBrowserActions(getJobTree(), refreshRssAction);
        installSearchActions(getSearchComponent());
    }

    protected void installBrowserActions(JTree jobTree, RefreshRssAction refreshRssAction) {
        DefaultActionGroup actionGroup = new DefaultActionGroup("JenkinsToolbarGroup", false);
        actionGroup.add(new SelectViewAction(this));
        actionGroup.add(new RefreshNodeAction(this));
        actionGroup.add(new RunBuildAction(this));
        actionGroup.add(new SortByStatusAction(this));
        actionGroup.add(refreshRssAction);
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

    protected void installSearchActions(JobSearchComponent searchComponent) {

        DefaultActionGroup actionGroup = new DefaultActionGroup("search bar", false);
        actionGroup.add(new PrevOccurrenceAction(searchComponent));
        actionGroup.add(new NextOccurrenceAction(searchComponent));

        ActionToolbar searchBar = ActionManager.getInstance().createActionToolbar("SearchBar", actionGroup, true);
        searchComponent.installSearchToolBar(searchBar);

        new OpenJobSearchPanelAction(this, this.getSearchComponent());
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
        jenkins = requestManager.loadJenkinsWorkspace(jenkinsAppSettings);
        fillData(jenkins);
    }

    public void loadView(View view) {
        if (view != null) {//TODO to be removed
            this.currentSelectedView = view;
        }
        ApplicationManager.getApplication().invokeLater(new LoadSelectedViewJob());
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

    private class LoadSelectedViewJob implements Runnable {
        @Override
        public void run() {
            if (currentSelectedView == null) {
                if (jenkins == null) {
                    return;
                }
                currentSelectedView = jenkins.getPrimaryView();
            }
            final List<Job> jobList;
            if (currentSelectedView instanceof FavoriteView) {
                jobList = requestManager.loadFavoriteJobs(jenkinsSettings.getFavoriteJobs());
            } else {
                jobList = requestManager.loadJenkinsView(currentSelectedView.getUrl());
            }
            jenkinsSettings.setLastSelectedView(currentSelectedView.getName());

            jenkins.setJobs(jobList);
            final BuildStatusAggregator buildStatusAggregator = new BuildStatusAggregator();

            GuiUtil.runInSwingThread(new Runnable() {
                @Override
                public void run() {
                    fillJobTree(buildStatusAggregator);
                    buildStatusAggregator.setNbJobs(jobList.size());
                    JenkinsWidget.getInstance(project).updateStatusIcon(buildStatusAggregator);
                }

            });
        }
    }
}
