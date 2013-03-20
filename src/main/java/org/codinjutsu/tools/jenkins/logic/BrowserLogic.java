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

package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.PopupHandler;
import com.intellij.util.IJSwingUtilities;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.model.FavoriteView;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.JobSearchComponent;
import org.codinjutsu.tools.jenkins.view.action.*;
import org.codinjutsu.tools.jenkins.view.action.search.NextOccurrenceAction;
import org.codinjutsu.tools.jenkins.view.action.search.OpenJobSearchPanelAction;
import org.codinjutsu.tools.jenkins.view.action.search.PrevOccurrenceAction;
import org.codinjutsu.tools.jenkins.view.action.settings.SortByStatusAction;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BrowserLogic implements Disposable {

    private final Project project;
    private final JenkinsAppSettings jenkinsAppSettings;
    private final JenkinsSettings jenkinsSettings;
    private final RequestManager requestManager;
    private final BrowserPanel browserPanel;

    private final Runnable refreshViewJob = new LoadSelectedViewJob();

    private ScheduledFuture<?> refreshViewFutureTask;

    private JobLoadListener jobLoadListener = JobLoadListener.NULL;

    private Jenkins jenkins;
    private FavoriteView favoriteView;
    private View currentSelectedView;

    public BrowserLogic(Project project, JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings, RequestManager requestManager, BrowserPanel browserPanel, JobLoadListener jobLoadListener) {
        this.project = project;
        this.jenkinsAppSettings = jenkinsAppSettings;
        this.jenkinsSettings = jenkinsSettings;
        this.requestManager = requestManager;
        this.browserPanel = browserPanel;
        this.jobLoadListener = jobLoadListener;
    }


    public void init(RefreshRssAction refreshRssAction) {
        initGui(refreshRssAction);
        reloadConfiguration();
    }


    public void reloadConfiguration() {
        if (!jenkinsAppSettings.isServerUrlSet()) {
            jobLoadListener.afterLoadingJobs(new BuildStatusAggregator());//TODO Crappy, need rewrite this
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

    public void loadSelectedJob() {
        Job job = getSelectedJob();
        Job updatedJob = requestManager.loadJob(job.getUrl());
        job.updateContentWith(updatedJob);
    }


    private void initGui(RefreshRssAction refreshRssAction) {
        browserPanel.createSearchPanel();
        installBrowserActions(browserPanel.getJobTree(), browserPanel, refreshRssAction);
        installSearchActions(browserPanel.getSearchComponent());
    }


    private void loadJenkinsWorkspace() {
        jenkins = requestManager.loadJenkinsWorkspace(jenkinsAppSettings);
        browserPanel.fillData(jenkins);
    }


    public void loadView(View view) {
        if (view != null) {//TODO to be removed
            this.currentSelectedView = view;
        }
        ApplicationManager.getApplication().invokeLater(new LoadSelectedViewJob());
    }

    public View getCurrentSelectedView() {
        return currentSelectedView;
    }

    public Jenkins getJenkins() {
        return jenkins;
    }


    public Job getSelectedJob() {
        return browserPanel.getSelectedJob();
    }


    protected void installBrowserActions(JTree jobTree, SimpleToolWindowPanel toolWindowPanel, RefreshRssAction refreshRssAction) {
        DefaultActionGroup actionGroup = new DefaultActionGroup("JenkinsToolbarGroup", false);
        actionGroup.add(new SelectViewAction(this));
        actionGroup.add(new RefreshNodeAction(this));
        actionGroup.add(new RunBuildAction(this));
        actionGroup.add(new SortByStatusAction(this));
        actionGroup.add(refreshRssAction);
        actionGroup.addSeparator();
        actionGroup.add(new OpenPluginSettingsAction());

        GuiUtil.installActionGroupInToolBar(actionGroup, toolWindowPanel, ActionManager.getInstance(), "jenkinsBrowserActions");

        DefaultActionGroup popupGroup = new DefaultActionGroup("JenkinsPopupAction", true);
        popupGroup.add(new SetJobAsFavoriteAction(this));
        popupGroup.add(new UnsetJobAsFavoriteAction(this));
        popupGroup.addSeparator();
        popupGroup.add(new GotoJobPageAction(browserPanel));
        popupGroup.add(new GotoLastBuildPageAction(browserPanel));

        installActionGroupInPopupMenu(popupGroup, jobTree, ActionManager.getInstance());
    }


    protected void installSearchActions(JobSearchComponent searchComponent) {

        DefaultActionGroup actionGroup = new DefaultActionGroup("search bar", false);
        actionGroup.add(new PrevOccurrenceAction(searchComponent));
        actionGroup.add(new NextOccurrenceAction(searchComponent));

        ActionToolbar searchBar = ActionManager.getInstance().createActionToolbar("SearchBar", actionGroup, true);
        searchComponent.installSearchToolBar(searchBar);

        new OpenJobSearchPanelAction(browserPanel, browserPanel.getSearchComponent());
    }


    void initScheduledJobs(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
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


    private void displayConnectionErrorMsg() {
        browserPanel.setErrorMsg(jenkinsAppSettings.getServerUrl(), "(Unable to connect. Check Jenkins Plugin Settings.)");
    }


    public RequestManager getJenkinsManager() {
        return requestManager;
    }

    private static void installActionGroupInPopupMenu(ActionGroup group,
                                                      JComponent component,
                                                      ActionManager actionManager) {
        if (actionManager == null) {
            return;
        }
        PopupHandler.installPopupHandler(component, group, "POPUP", actionManager);
    }

    public BrowserPanel getBrowserPanel() {
        return browserPanel;
    }


    @Override
    public void dispose() {

    }

    public void setAsFavorite(Job job) {
        jenkinsSettings.addFavorite(job);
        createFavoriteViewIfNecessary();
        browserPanel.update();

    }

    private void createFavoriteViewIfNecessary() {
        if (favoriteView == null) {
            favoriteView = FavoriteView.create();
        }
    }

    public void removeFavorite(Job selectedJob) {
        jenkinsSettings.removeFavorite(selectedJob);
        browserPanel.update();
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
                    browserPanel.fillJobTree(buildStatusAggregator);
                    buildStatusAggregator.setNbJobs(jobList.size());
                    jobLoadListener.afterLoadingJobs(buildStatusAggregator);
                }

            });
        }
    }

    public interface JobLoadListener {

        void afterLoadingJobs(BuildStatusAggregator buildStatusAggregator);

        JobLoadListener NULL = new JobLoadListener() {
            @Override
            public void afterLoadingJobs(BuildStatusAggregator buildStatusAggregator) {
            }
        };
    }
}
