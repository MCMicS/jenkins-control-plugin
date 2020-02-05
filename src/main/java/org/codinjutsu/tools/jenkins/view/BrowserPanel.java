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
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.JenkinsToolWindowFactory;
import org.codinjutsu.tools.jenkins.JenkinsWindowManager;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.*;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BrowserPanel extends SimpleToolWindowPanel {

    private static final Logger logger = Logger.getLogger(BrowserPanel.class);

    private static final String UNAVAILABLE = "No Jenkins server available";

    private static final String LOADING = "Loading...";
    private static final JobNameComparator JOB_NAME_COMPARATOR = new JobNameComparator();
    private static final Comparator<Job> sortByStatusComparator = Comparator.comparing(BrowserPanel::toBuildStatus);
    private static final Comparator<Job> sortByNameComparator = Comparator.comparing(Job::getName, JOB_NAME_COMPARATOR);
    private final Tree jobTree;
    private final Runnable refreshViewJob;
    private final Project project;
    private final JenkinsAppSettings jenkinsAppSettings;
    private final JenkinsSettings jenkinsSettings;
    private final RequestManager requestManager;
    private final Jenkins jenkins;
    private final Map<String, Job> watchedJobs = new ConcurrentHashMap<>();
    private JPanel rootPanel;
    private JPanel jobPanel;
    private boolean sortedByBuildStatus;
    private ScheduledFuture<?> refreshViewFutureTask;
    private FavoriteView favoriteView;
    private View currentSelectedView;

    public BrowserPanel(final Project project) {

        super(true);
        this.project = project;

        refreshViewJob = () -> GuiUtil.runInSwingThread(new LoadSelectedViewJob(project));

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

    @NotNull
    private static BuildStatusEnum toBuildStatus(Job job) {
        return BuildStatusEnum.getStatus(job.getColor());
    }

    public static BrowserPanel getInstance(Project project) {
        return ServiceManager.getService(project, BrowserPanel.class);
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

    @NotNull
    private static Comparator<DefaultMutableTreeNode> wrapJobSorter(Comparator<Job> jobComparator) {
        return (node1, node2) -> {
            if (node1.getUserObject() instanceof Job && node2.getUserObject() instanceof Job) {
                Job job1 = ((Job) node1.getUserObject());
                Job job2 = ((Job) node2.getUserObject());

                return jobComparator.compare(job1, job2);
            }
            return 0;
        };
    }

    /*whole method could be moved inside of ExecutorProvider (executor would expose interface that would allow to schedule
      new task previously cancelling previous ones) */
    public void initScheduledJobs() {
        final ExecutorService executorService = ExecutorService.getInstance(project);
        final ScheduledThreadPoolExecutor executor = executorService.getExecutor();
        executorService.safeTaskCancel(refreshViewFutureTask);
        executor.remove(refreshViewJob);

        if (jenkinsAppSettings.isServerUrlSet() && jenkinsAppSettings.getJobRefreshPeriod() > 0) {
            refreshViewFutureTask = executor.scheduleWithFixedDelay(refreshViewJob, jenkinsAppSettings.getJobRefreshPeriod(), jenkinsAppSettings.getJobRefreshPeriod(), TimeUnit.MINUTES);
        }
    }

    public Build getSelectedBuild() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jobTree.getLastSelectedPathComponent();
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof Build) {
                return (Build) userObject;
            }
        }
        return null;
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

    public void setSortedByStatus(boolean sortedByBuildStatus) {
        this.sortedByBuildStatus = sortedByBuildStatus;
        final DefaultTreeModel model = (DefaultTreeModel) jobTree.getModel();
        if (sortedByBuildStatus) {
            TreeUtil.sort(model, wrapJobSorter(sortByStatusComparator));
        } else {
            TreeUtil.sort(model, wrapJobSorter(sortByNameComparator));
        }

        GuiUtil.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                model.nodeStructureChanged((TreeNode) model.getRoot());
            }
        });
    }

    private void update() {
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
        if (SwingUtilities.isEventDispatchThread()) {
            logger.warn("BrowserPanel.loadSelectedJob called from EDT");
        }
        final Job job = getSelectedJob();
        loadJob(job);

    }

    public void loadJob(final Job job) {
        if (!SwingUtilities.isEventDispatchThread()) {
            logger.warn("BrowserPanel.loadJob called from outside of EDT");
        }
        GuiUtil.runInSwingThread(new Task.Backgroundable(project, "Loading job", true, JenkinsLoadingTaskOption.INSTANCE) {

            private Job returnJob;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                returnJob = requestManager.loadJob(job);
            }

            @Override
            public void onSuccess() {
                job.updateContentWith(returnJob);
                updateJobNode(job);
            }

        });
    }

    public void refreshJob(Job job) {
        updateJobNode(job);
    }

    private void updateJobNode(Job job) {
        final DefaultTreeModel model = (DefaultTreeModel) jobTree.getModel();
        final Object modelRoot = model.getRoot();
        final int childCount = model.getChildCount(modelRoot);
        for (int i = 0; i < childCount; ++i) {
            Object child = model.getChild(modelRoot, i);
            if (child instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child;
                if (childNode.getUserObject() == job) {
                    model.nodeChanged(childNode);
                    fillBuildsTree(job, childNode);
                    break;
                }
            }
        }
    }

    public boolean hasFavoriteJobs() {
        return !jenkinsSettings.getFavoriteJobs().isEmpty();
    }

    public void notifyInfoJenkinsToolWindow(final String htmlLinkMessage) {
        ToolWindowManager.getInstance(project).notifyByBalloon(
                JenkinsToolWindowFactory.JENKINS_BROWSER,
                MessageType.INFO,
                htmlLinkMessage,
                null,
                new BrowserHyperlinkListener());
    }

    public void notifyErrorJenkinsToolWindow(final String message) {
        GuiUtil.runInSwingThread(() -> ToolWindowManager.getInstance(project).notifyByBalloon(
                JenkinsToolWindowFactory.JENKINS_BROWSER, MessageType.ERROR, message));
    }

    private Tree createTree(final List<JenkinsSettings.FavoriteJob> favoriteJobs) {

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


    public void handleEmptyConfiguration() {
        JenkinsWidget.getInstance(project).updateStatusIcon(BuildStatusAggregator.EMPTY); //FIXME could be handled elsehwere
        DefaultTreeModel model = (DefaultTreeModel) jobTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        root.removeAllChildren();
        model.nodeStructureChanged(root);
        jobTree.setRootVisible(false);

        jenkins.update(Jenkins.byDefault());

        currentSelectedView = null;
        setJobsUnavailable();
    }

    public void setJobsUnavailable() {
        jobTree.getEmptyText().setText(UNAVAILABLE);
    }

    public void postAuthenticationInitialization() {
        if (!jenkinsSettings.getFavoriteJobs().isEmpty()) {
            createFavoriteViewIfNecessary();
        }

        String lastSelectedViewName = jenkinsSettings.getLastSelectedView();
        View viewToLoad;
        if (StringUtils.isEmpty(lastSelectedViewName)) {
            viewToLoad = jenkins.getPrimaryView();
        } else if (favoriteView != null && lastSelectedViewName.equals(favoriteView.getName())) {
            viewToLoad = favoriteView;
        } else {
            viewToLoad = jenkins.getViewByName(lastSelectedViewName);
        }
        loadView(viewToLoad);
    }

    public void init() {
        initGui();
        if (!isConfigured()) { //run when there is not configuration
            handleEmptyConfiguration();
        }
    }

    private void initGui() {
        installActionsInToolbar();
        installActionsInPopupMenu();
    }

    private void installActionsInToolbar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup("JenkinsToolbarGroup", false);
        actionGroup.add(new SelectViewAction(this));
        actionGroup.add(new RefreshNodeAction(this));
        actionGroup.add(new LoadBuildsAction(this));
        actionGroup.add(new RunBuildAction(this));
        actionGroup.add(new StopBuildAction(this));
        actionGroup.add(new SortByStatusAction(this));
        actionGroup.add(new RefreshRssAction());
        actionGroup.addSeparator();
        actionGroup.add(new OpenPluginSettingsAction());


        GuiUtil.installActionGroupInToolBar(actionGroup, this, ActionManager.getInstance(), "jenkinsBrowserActions");
    }

    private void installActionsInPopupMenu() {
        DefaultActionGroup popupGroup = new DefaultActionGroup("JenkinsPopupAction", true);

        popupGroup.add(new RunBuildAction(this));
        popupGroup.add(new StopBuildAction(this));
        popupGroup.add(new ShowLogAction(this));
        popupGroup.addSeparator();
        popupGroup.add(new SetJobAsFavoriteAction(this));

        popupGroup.add(new UnsetJobAsFavoriteAction(this));
        popupGroup.addSeparator();
        popupGroup.add(new GotoJobPageAction(this));
        popupGroup.add(new GotoBuildPageAction(this));
        popupGroup.add(new GotoBuildConsolePageAction(this));
        popupGroup.add(new GotoBuildTestResultsPageAction(this));
        popupGroup.add(new GotoLastBuildPageAction(this));
        popupGroup.addSeparator();
        popupGroup.add(new UploadPatchToJob(this));

        PopupHandler.installPopupHandler(jobTree, popupGroup, "POPUP", ActionManager.getInstance());
    }

    public void loadView(final View view) {
        this.currentSelectedView = view;
        if (!SwingUtilities.isEventDispatchThread()) {
            logger.warn("BrowserPanel.loadView called from outside EDT");
        }
        new LoadSelectedViewJob(project).queue();
    }

    public void refreshCurrentView() {
        if (!SwingUtilities.isEventDispatchThread()) {
            logger.warn("BrowserPanel.refreshCurrentView called outside EDT");
        }
        new LoadSelectedViewJob(project).queue();
    }

    private void loadJobs() {
        if (SwingUtilities.isEventDispatchThread()) {
            logger.warn("BrowserPanel.loadJobs called from EDT");
        }
        final List<Job> jobList;
        if (currentSelectedView instanceof FavoriteView) {
            jobList = requestManager.loadFavoriteJobs(jenkinsSettings.getFavoriteJobs());
        } else {
            jobList = requestManager.loadJenkinsView(currentSelectedView);
        }

        jenkinsSettings.setLastSelectedView(currentSelectedView.getName());

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

    private void fillBuildsTree(Job job, DefaultMutableTreeNode jobNode) {
        if (!job.getLastBuilds().isEmpty()) {
            for (Build build : job.getLastBuilds()) {
                DefaultMutableTreeNode buildNode = new DefaultMutableTreeNode(build);
                jobNode.add(buildNode);
            }
        }
    }

    private void fillJobTree(final BuildStatusVisitor buildStatusVisitor) {
        final List<Job> jobList = jenkins.getJobs();
        if (jobList.isEmpty()) {
            return;
        }

        DefaultTreeModel model = (DefaultTreeModel) jobTree.getModel();
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        rootNode.removeAllChildren();

        for (Job job : jobList) {
            DefaultMutableTreeNode jobNode = new DefaultMutableTreeNode(job);
            if (job.isFetchBuild()) {
                fillBuildsTree(job, jobNode);
            }
            rootNode.add(jobNode);
            visit(job, buildStatusVisitor);
        }

        watch();

        setSortedByStatus(sortedByBuildStatus);

        jobTree.setRootVisible(true);
    }

    public void setAsFavorite(final List<Job> jobs) {
        jenkinsSettings.addFavorite(jobs);
        createFavoriteViewIfNecessary();
        update();
    }

    public void removeFavorite(final List<Job> selectedJobs) {
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

    public boolean isAFavoriteJob(final String jobName) {
        return jenkinsSettings.isAFavoriteJob(jobName);
    }

    private void createFavoriteViewIfNecessary() {
        if (favoriteView == null) {
            favoriteView = FavoriteView.create();
        }
    }

    private void setTreeBusy(final boolean isBusy) {
        GuiUtil.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                jobTree.setPaintBusy(isBusy);
            }
        });

    }

    public boolean isConfigured() {
        return jenkinsAppSettings.isServerUrlSet();
    }

    public void updateWorkspace(Jenkins jenkinsWorkspace) {
        jenkins.update(jenkinsWorkspace);
    }

    public void addToWatch(String changeListName, Job job) {
        JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);
        Build build = job.getLastBuild();
        build.setNumber(build.getNumber() + 1);
        build.setUrl(settings.getServerUrl() + String.format("/job/%s/%d/", job.getName(), build.getNumber()));
        watchedJobs.put(changeListName, job);
    }

    private void watch() {
        if (!SwingUtilities.isEventDispatchThread()) {
            logger.warn("BrowserPanel.watch called from outside EDT");
        }
        if (!watchedJobs.isEmpty()) {
            for (final Map.Entry<String, Job> entry : watchedJobs.entrySet()) {
                final Job job = entry.getValue();
                final Build lastBuild = job.getLastBuild();
                new Task.Backgroundable(project, "Jenkins build watch", true, JenkinsLoadingTaskOption.INSTANCE) {

                    private Build build;

                    @Override
                    public void onSuccess() {
                        if (lastBuild.isBuilding() && !build.isBuilding()) {
                            notifyInfoJenkinsToolWindow(String.format("Status of build for Changelist \"%s\" is %s", entry, build.getStatus().getStatus()));
                        }
                        job.setLastBuild(build);
                    }

                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        indicator.setIndeterminate(true);
                        build = requestManager.loadBuild(lastBuild);
                    }
                }.queue();
            }
        }
    }

    public Map<String, Job> getWatched() {
        return watchedJobs;
    }

    private class LoadSelectedViewJob extends Task.Backgroundable {
        public LoadSelectedViewJob(@Nullable Project project) {
            super(project, "Loading Jenkins Jobs", true, JenkinsLoadingTaskOption.INSTANCE);
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            indicator.setIndeterminate(true);
            try {
                setTreeBusy(true);
                View viewToLoad = getViewToLoad();
                if (viewToLoad == null) {
                    return;
                }
                currentSelectedView = viewToLoad;
                loadJobs();

            } catch (ConfigurationException ex) {
                notifyErrorJenkinsToolWindow(ex.getMessage());
            } finally {
                setTreeBusy(false);
            }
        }

        @Override
        public void onSuccess() {
            final BuildStatusAggregator buildStatusAggregator = new BuildStatusAggregator(jenkins.getJobs().size());

            GuiUtil.runInSwingThread(new Runnable() {
                @Override
                public void run() {
                    fillJobTree(buildStatusAggregator);
                    JenkinsWidget.getInstance(project).updateStatusIcon(buildStatusAggregator);
                }
            });

        }
    }
}
