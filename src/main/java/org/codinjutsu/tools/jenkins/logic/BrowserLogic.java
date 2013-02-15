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
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.PopupHandler;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class BrowserLogic implements Disposable {

    private final JenkinsAppSettings jenkinsAppSettings;
    private final JenkinsSettings jenkinsSettings;
    private final RequestManager requestManager;
    private final BrowserPanel browserPanel;

    private final Runnable refreshViewJob = new LoadSelectedViewJob();

    private ScheduledFuture<?> refreshViewFutureTask;

    private JobLoadListener jobLoadListener = JobLoadListener.NULL;

    private Jenkins jenkins;

    public BrowserLogic(JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings, RequestManager requestManager, BrowserPanel browserPanel, JobLoadListener jobLoadListener) {
        this.jenkinsAppSettings = jenkinsAppSettings;
        this.jenkinsSettings = jenkinsSettings;
        this.requestManager = requestManager;
        this.browserPanel = browserPanel;
        this.jobLoadListener = jobLoadListener;
    }


    public void init(RefreshRssAction refreshRssAction) {
        initGui(refreshRssAction);
        reloadConfiguration();
        initListeners();
    }


    public void reloadConfiguration() {
        if (!jenkinsAppSettings.isServerUrlSet()) {
            jobLoadListener.afterLoadingJobs(new BuildStatusAggregator());//TODO Crappy, need rewrite this
            clearViewCombo();
//            displayMissingConfiguration();
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
        if (StringUtils.isNotEmpty(lastSelectedViewName)) {
            browserPanel.selectView(lastSelectedViewName);
        } else {
            View primaryView = jenkins.getPrimaryView();
            if (primaryView != null) {
                browserPanel.selectView(primaryView.getName());
            }
        }
        loadSelectedView();
    }

    private void clearViewCombo() {
        browserPanel.resetViewCombo(Collections.<View>emptyList());
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


    public void loadSelectedView() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new LoadSelectedViewJob());
        executorService.shutdown();
    }


    public Jenkins getJenkins() {
        return browserPanel.getJenkins();
    }


    public Job getSelectedJob() {
        return browserPanel.getSelectedJob();
    }


    protected void installBrowserActions(JTree jobTree, SimpleToolWindowPanel toolWindowPanel, RefreshRssAction refreshRssAction) {
        DefaultActionGroup actionGroup = new DefaultActionGroup("JenkinsToolbarGroup", false);
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


//    private void displayMissingConfiguration() {
//        browserPanel.setErrorMsg(jenkinsAppSettings.getServerUrl(), "(Missing configuration. Check Jenkins Plugin Settings.)");
//    }
//
//
    private void displayConnectionErrorMsg() {
        browserPanel.setErrorMsg(jenkinsAppSettings.getServerUrl(), "(Unable to connect. Check Jenkins Plugin Settings.)");
    }


    private View getSelectedJenkinsView() {
        return browserPanel.getSelectedJenkinsView();
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


    private void initListeners() {
        browserPanel.getViewCombo().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    loadSelectedView();
                }
            }
        });
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
        FavoriteView favoriteView = browserPanel.getFavoriteView();
        if (favoriteView == null) {
            favoriteView = FavoriteView.create();
            browserPanel.updateViewCombo(favoriteView);
        }
    }

    public void removeFavorite(Job selectedJob) {
        jenkinsSettings.removeFavorite(selectedJob);
        browserPanel.update();
        if (jenkinsSettings.isFavoriteViewEmpty() && getSelectedJenkinsView() instanceof FavoriteView) {
            browserPanel.resetViewCombo(jenkins.getViews());
            browserPanel.getViewCombo().getModel().setSelectedItem(jenkins.getPrimaryView());
        } else {
            View selectedJenkinsView = getSelectedJenkinsView();
            if (selectedJenkinsView instanceof FavoriteView) {
                loadSelectedView();
            }
        }
    }

    public boolean isAFavoriteJob(String jobName) {
        return jenkinsSettings.isAFavoriteJob(jobName);
    }

    private class LoadSelectedViewJob implements Runnable {
        @Override
        public void run() {
            View jenkinsView = getSelectedJenkinsView();
            if (jenkinsView == null) {
                if (jenkins == null) {
                    return;
                }
                jenkinsView = jenkins.getPrimaryView();
            }
            final View selectedView = jenkinsView;

            final List<Job> jobList;

            if (selectedView instanceof FavoriteView) {
                jobList = requestManager.loadFavoriteJobs(jenkinsSettings.getFavoriteJobs());
            } else {
                jobList = requestManager.loadJenkinsView(selectedView.getUrl());
            }
            jenkinsSettings.setLastSelectedView(selectedView.getName());

            jenkins.setJobs(jobList);
            final BuildStatusAggregator buildStatusAggregator = new BuildStatusAggregator();

            GuiUtil.runInSwingThread(new Runnable() {
                @Override
                public void run() {
                    browserPanel.fillJobTree(jenkins, buildStatusAggregator);
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
