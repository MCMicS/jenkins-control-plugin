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
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BrowserPanel extends SimpleToolWindowPanel implements Disposable {

    private static final String UNAVAILABLE = "No Jenkins server available";

    private static final String LOADING = "Loading...";

    private JPanel rootPanel;

    private JPanel jobPanel;
    private Tree jobTree;

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

    private Map<String, Job> watchedJobs = new HashMap<String, Job>();

    private static final Comparator<DefaultMutableTreeNode> sortByStatusComparator = new Comparator<DefaultMutableTreeNode>() {
        @Override
        public int compare(DefaultMutableTreeNode treeNode1, DefaultMutableTreeNode treeNode2) {
            Job job1 = ((Job) treeNode1.getUserObject());
            Job job2 = ((Job) treeNode2.getUserObject());

            return new Integer(BuildStatusEnum.getStatus(job1.getColor()).ordinal()).compareTo(BuildStatusEnum.getStatus(job2.getColor()).ordinal());
        }
    };
    private static final Comparator<DefaultMutableTreeNode> sortByNameComparator = new Comparator<DefaultMutableTreeNode>() {
        @Override
        public int compare(DefaultMutableTreeNode treeNode1, DefaultMutableTreeNode treeNode2) {
            Job job1 = ((Job) treeNode1.getUserObject());
            Job job2 = ((Job) treeNode2.getUserObject());

            return job1.getName().compareTo(job2.getName());
        }
    };


    public static BrowserPanel getInstance(Project project) {
        return ServiceManager.getService(project, BrowserPanel.class);
    }

    public BrowserPanel(final Project project) {

        super(true);
        this.project = project;

        refreshViewJob = new Runnable() {
            @Override
            public void run() {
                new LoadSelectedViewJob(project).queue();
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

    public void initScheduledJobs(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        safeTaskCancel(refreshViewFutureTask);
        scheduledThreadPoolExecutor.remove(refreshViewJob);

        if (jenkinsAppSettings.isServerUrlSet() && jenkinsAppSettings.getJobRefreshPeriod() > 0) {
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

    public List<Job> getJobs() {
        return jenkins.getJobs();
    }

    public Job getJob(String name) {
        List<Job> jobs = jenkins.getJobs();
        if (jobs.size() > 0) {
            for (Job job : jobs) {
                if (job.getName().equals(name)) {
                    return job;
                }
            }
        }
        return null;
    }

    public void setSortedByStatus(boolean selected) {
        sortedByBuildStatus = selected;
        final DefaultTreeModel model = (DefaultTreeModel) jobTree.getModel();
        if (selected) {
            TreeUtil.sort(model, sortByStatusComparator);
        } else {
            TreeUtil.sort(model, sortByNameComparator);
        }

        GuiUtil.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                model.nodeStructureChanged((TreeNode) model.getRoot());
            }
        });
    }

    @Override
    public void dispose() {
        ToolWindowManager.getInstance(project).unregisterToolWindow(JenkinsWindowManager.JENKINS_BROWSER);
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

    public Job loadJob(Job job) {
        Job updatedJob = requestManager.loadJob(job.getUrl());
        job.updateContentWith(updatedJob);
        return updatedJob;
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

    private Tree createTree(List<JenkinsSettings.FavoriteJob> favoriteJobs) {

        SimpleTree tree = new SimpleTree();
        tree.getEmptyText().setText(LOADING);
        tree.setCellRenderer(new JenkinsTreeRenderer(favoriteJobs));
        tree.setName("jobTree");
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(jenkins)));

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
            JenkinsWidget.getInstance(project).updateStatusIcon(BuildStatusAggregator.EMPTY);
            DefaultTreeModel model = (DefaultTreeModel) jobTree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            root.removeAllChildren();
            model.nodeStructureChanged(root);
            jobTree.setRootVisible(false);

            jenkins.update(Jenkins.byDefault());

            currentSelectedView = null;
            jobTree.getEmptyText().setText(UNAVAILABLE);
            return;
        }

        try {
            requestManager.authenticate(jenkinsAppSettings, jenkinsSettings);
        } catch (ConfigurationException ex) {
            jobTree.getEmptyText().setText(UNAVAILABLE);
            throw ex;
        }

        this.jenkins.update(requestManager.loadJenkinsWorkspace(jenkinsAppSettings));

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
        installActionsInToolbar();

        installActionsInPopupMenu();
    }

    private void installActionsInToolbar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup("JenkinsToolbarGroup", false);
        actionGroup.add(new SelectViewAction(this));
        actionGroup.add(new RefreshNodeAction(this));
        actionGroup.add(new RunBuildAction(this));
        actionGroup.add(new SortByStatusAction(this));
        actionGroup.add(new RefreshRssAction());
        actionGroup.addSeparator();
        actionGroup.add(new OpenPluginSettingsAction());

        GuiUtil.installActionGroupInToolBar(actionGroup, this, ActionManager.getInstance(), "jenkinsBrowserActions");
    }

    private void installActionsInPopupMenu() {
        DefaultActionGroup popupGroup = new DefaultActionGroup("JenkinsPopupAction", true);
        popupGroup.add(new SetJobAsFavoriteAction(this));
        popupGroup.add(new UnsetJobAsFavoriteAction(this));
        popupGroup.addSeparator();
        popupGroup.add(new GotoJobPageAction(this));
        popupGroup.add(new GotoLastBuildPageAction(this));
        popupGroup.addSeparator();
        popupGroup.add(new UploadPatchToJob(this));

        PopupHandler.installPopupHandler(jobTree, popupGroup, "POPUP", ActionManager.getInstance());
    }

    public void loadView(View view) {
        this.currentSelectedView = view;
        new LoadSelectedViewJob(project).queue();
    }

    public void refreshCurrentView() {
        new LoadSelectedViewJob(project).queue();
    }

    private void loadJobs(View viewToLoad) {
        final List<Job> jobList;
        if (currentSelectedView instanceof FavoriteView) {
            jobList = requestManager.loadFavoriteJobs(jenkinsSettings.getFavoriteJobs());
        } else {
            jobList = requestManager.loadJenkinsView(viewToLoad.getUrl());
        }

        jenkinsSettings.setLastSelectedView(viewToLoad.getName());

        jenkins.setJobs(jobList);
    }

    private View getViewToLoad() {
        if (currentSelectedView != null) {
            return currentSelectedView;
        }

        View primaryView = jenkins.getPrimaryView();
        if (primaryView != null) {
            return primaryView;
        }

        return null;
    }


    public void fillJobTree(BuildStatusVisitor buildStatusVisitor) {
        List<Job> jobList = jenkins.getJobs();
        if (jobList.isEmpty()) {
            return;
        }

        DefaultTreeModel model = (DefaultTreeModel) jobTree.getModel();
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        rootNode.removeAllChildren();

        for (Job job : jobList) {
            DefaultMutableTreeNode jobNode = new DefaultMutableTreeNode(job);
            rootNode.add(jobNode);
            visit(job, buildStatusVisitor);
        }

        watch();

        setSortedByStatus(sortedByBuildStatus);

        jobTree.setRootVisible(true);
    }

    public void setAsFavorite(List<Job> jobs) {
        jenkinsSettings.addFavorite(jobs);
        createFavoriteViewIfNecessary();
        update();
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

    private void createFavoriteViewIfNecessary() {
        if (favoriteView == null) {
            favoriteView = FavoriteView.create();
        }
    }

    void setTreeBusy(final boolean isBusy) {
        GuiUtil.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                jobTree.setPaintBusy(isBusy);
            }
        });

    }

    private class LoadSelectedViewJob extends Task.Backgroundable {
        public LoadSelectedViewJob(@Nullable Project project) {
            super(project, "Loading Jenkins Jobs", true, JenkinsLoadingTaskOption.INSTANCE);
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {

            try {
                setTreeBusy(true);
                View viewToLoad = getViewToLoad();
                if (viewToLoad == null) {
                    return;
                }
                currentSelectedView = viewToLoad;
                loadJobs(viewToLoad);

                final BuildStatusAggregator buildStatusAggregator = new BuildStatusAggregator(jenkins.getJobs().size());

                GuiUtil.runInSwingThread(new Runnable() {
                    @Override
                    public void run() {
                        fillJobTree(buildStatusAggregator);
                        JenkinsWidget.getInstance(project).updateStatusIcon(buildStatusAggregator);
                    }
                });
            } finally {
                setTreeBusy(false);
            }
        }
    }

    public void addToWatch(String changeListName, Job job) {
        JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);
        Build build = job.getLastBuild();
        build.setNumber(build.getNumber() + 1);
        build.setUrl(settings.getServerUrl() + String.format("/job/%s/%d/", job.getName(), build.getNumber()));
        watchedJobs.put(changeListName, job);
    }

    public void watch() {
        if (watchedJobs.size() > 0) {
            for (String key : watchedJobs.keySet()) {
                Job job = watchedJobs.get(key);
                Build lastBuild = job.getLastBuild();
                Build build = requestManager.loadBuild(lastBuild.getUrl());
                if (lastBuild.isBuilding() && !build.isBuilding()) {
                    notifyInfoJenkinsToolWindow(String.format("Status of build for Changelist \"%s\" is %s", key, build.getStatus().getStatus()));
                }
                job.setLastBuild(build);
            }
        }
    }

    public Map<String, Job> getWatched() {
        return watchedJobs;
    }

}