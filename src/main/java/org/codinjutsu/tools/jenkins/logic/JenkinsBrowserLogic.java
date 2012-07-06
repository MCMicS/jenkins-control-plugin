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
import com.intellij.ui.PopupHandler;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserPanel;
import org.codinjutsu.tools.jenkins.view.JobSearchComponent;
import org.codinjutsu.tools.jenkins.view.RssLatestBuildPanel;
import org.codinjutsu.tools.jenkins.view.action.*;
import org.codinjutsu.tools.jenkins.view.action.search.NextOccurrenceAction;
import org.codinjutsu.tools.jenkins.view.action.search.OpenJobSearchPanelAction;
import org.codinjutsu.tools.jenkins.view.action.search.PrevOccurrenceAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JenkinsBrowserLogic implements Disposable {

    private static final String JENKINS_JOB_ACTION_GROUP = "JenkinsJobGroup";
    private static final String JENKINS_RSS_ACTIONS = "JenkinsRssActions";
    private static final String JENKINS_ACTIONS = "jenkinsBrowserActions";

    private final JenkinsConfiguration configuration;
    private final JenkinsRequestManager jenkinsRequestManager;

    private final BuildStatusListener buildStatusListener;

    private Jenkins jenkins;
    private final Map<String, Build> currentBuildMap = new HashMap<String, Build>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);

    private Runnable refreshViewJob = new LoadSelectedViewJob();
    private Runnable refreshRssBuildsJob = new LoadLatestBuildsJob(true);

    private final JenkinsBrowserPanel jenkinsBrowserPanel;
    private final RssLatestBuildPanel rssLatestJobPanel;

    private JobLoadListener jobLoadListener = JobLoadListener.NULL;


    public JenkinsBrowserLogic(JenkinsConfiguration configuration, JenkinsRequestManager jenkinsRequestManager, JenkinsBrowserPanel jenkinsBrowserPanel, RssLatestBuildPanel rssLatestJobPanel, BuildStatusListener buildStatusListener, JobLoadListener jobLoadListener) {
        this.configuration = configuration;
        this.jenkinsRequestManager = jenkinsRequestManager;
        this.jenkinsBrowserPanel = jenkinsBrowserPanel;
        this.rssLatestJobPanel = rssLatestJobPanel;
        this.buildStatusListener = buildStatusListener;
        this.jobLoadListener = jobLoadListener;
    }


    public void init() {
        initGui();
        reloadConfiguration();
        initListeners();
    }


    public void reloadConfiguration() {
        if (!configuration.isServerUrlSet()) {
            jobLoadListener.afterLoadingJobs(new BuildStatusAggregator());//TODO Crappy, need rewrite this
            displayMissingConfiguration();
            return;
        }

        try {
            jenkinsRequestManager.authenticate(configuration.getServerUrl(), configuration.getSecurityMode(), configuration.getUsername(), configuration.getPasswordFile(), configuration.getCrumbFile());
        } catch (Exception ex) {
            displayConnectionErrorMsg();
            return;
        }

        loadJenkinsWorkspace();

        String lastSelectedViewName = configuration.getBrowserPreferences().getLastSelectedView();
        if (StringUtils.isNotEmpty(lastSelectedViewName)) {
            jenkinsBrowserPanel.getViewByName(lastSelectedViewName);
        }

        loadSelectedView();

        loadLatestBuilds(false);

        cleanRssEntries();

        initScheduledJobs();
    }


    public void loadSelectedJob() {
        Job job = getSelectedJob();
        Job updatedJob = jenkinsRequestManager.loadJob(job.getUrl());
        job.updateContentWith(updatedJob);
    }


    private void initGui() {
        jenkinsBrowserPanel.createSearchPanel();
        installRssActions(rssLatestJobPanel.getRssActionPanel());
        installBrowserActions(jenkinsBrowserPanel.getJobTree(), jenkinsBrowserPanel.getActionPanel());
        installSearchActions(jenkinsBrowserPanel.getSearchComponent());
    }


    public void cleanRssEntries() {
        rssLatestJobPanel.cleanRssEntries();
    }


    private void loadJenkinsWorkspace() {
        jenkins = jenkinsRequestManager.loadJenkinsWorkspace(configuration);
        jenkinsBrowserPanel.fillData(jenkins);
    }


    public void loadSelectedView() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new LoadSelectedViewJob());
        executorService.shutdown();
    }


    public Jenkins getJenkins() {
        return jenkinsBrowserPanel.getJenkins();
    }


    public Job getSelectedJob() {
        return jenkinsBrowserPanel.getSelectedJob();
    }


    protected void installRssActions(JPanel rssActionPanel) {
        DefaultActionGroup actionGroup = new DefaultActionGroup(JENKINS_RSS_ACTIONS, true);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(new RefreshRssAction(this));
            actionGroup.addSeparator();
            actionGroup.add(new CleanRssAction(this));
        }
        installActionGroupInToolBar(actionGroup, rssActionPanel, ActionManager.getInstance(), JENKINS_RSS_ACTIONS);
    }


    protected void installBrowserActions(JTree jobTree, JPanel toolBar) {
        DefaultActionGroup actionGroup = new DefaultActionGroup(JENKINS_JOB_ACTION_GROUP, true);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(new RefreshNodeAction(this));
            actionGroup.addSeparator();
            actionGroup.add(new RunBuildAction(this));
//            actionGroup.add(new SetJobAsFavoriteAction(this));
//            actionGroup.add(new UnsetJobAsFavoriteAction(this));
            actionGroup.addSeparator();
            actionGroup.add(new GotoJobPageAction(jenkinsBrowserPanel));
            actionGroup.add(new GotoLastBuildPageAction(jenkinsBrowserPanel));
            actionGroup.addSeparator();
            actionGroup.add(new OpenPluginSettingsAction());
        }
        installActionGroupInToolBar(actionGroup, toolBar, ActionManager.getInstance(), JENKINS_ACTIONS);
        installActionGroupInPopupMenu(actionGroup, jobTree, ActionManager.getInstance());
    }


    protected void installSearchActions(JobSearchComponent searchComponent) {

        DefaultActionGroup actionGroup = new DefaultActionGroup("search bar", false);
        actionGroup.add(new PrevOccurrenceAction(searchComponent));
        actionGroup.add(new NextOccurrenceAction(searchComponent));

        ActionToolbar searchBar = ActionManager.getInstance().createActionToolbar("SearchBar", actionGroup, true);
        searchComponent.installSearchToolBar(searchBar);

        new OpenJobSearchPanelAction(jenkinsBrowserPanel, jenkinsBrowserPanel.getSearchComponent());
    }


    private Entry<String, Build> getFirstFailedBuild(Map<String, Build> finishedBuilds) {
        for (Entry<String, Build> buildByJobName : finishedBuilds.entrySet()) {
            Build build = buildByJobName.getValue();
            if (build.getStatus() == BuildStatusEnum.FAILURE) {
                return buildByJobName;
            }
        }
        return null;
    }


    private Map<String, Build> loadAndReturnNewLatestBuilds() {
        Map<String, Build> latestBuildMap = jenkinsRequestManager.loadJenkinsRssLatestBuilds(configuration);
        Map<String, Build> newBuildMap = new HashMap<String, Build>();
        for (Entry<String, Build> entry : latestBuildMap.entrySet()) {
            String jobName = entry.getKey();
            Build newBuild = entry.getValue();
            Build currentBuild = currentBuildMap.get(jobName);
            if (!currentBuildMap.containsKey(jobName) || newBuild.isAfter(currentBuild)) {
                currentBuildMap.put(jobName, newBuild);
                newBuildMap.put(jobName, newBuild);
            }
        }

        return newBuildMap;
    }


    private void initScheduledJobs() {
        scheduledThreadPoolExecutor.remove(refreshRssBuildsJob);
        scheduledThreadPoolExecutor.remove(refreshViewJob);

        if (configuration.isEnableJobAutoRefresh()) {
            scheduledThreadPoolExecutor.scheduleAtFixedRate(refreshViewJob, 2, configuration.getJobRefreshPeriod(), TimeUnit.MINUTES);
        }

        if (configuration.isEnableRssAutoRefresh()) {
            scheduledThreadPoolExecutor.scheduleAtFixedRate(refreshRssBuildsJob, 2, configuration.getRssRefreshPeriod(), TimeUnit.MINUTES);
        }
    }


    private void displayMissingConfiguration() {
        jenkinsBrowserPanel.setErrorMsg(configuration.getServerUrl(), "(Missing configuration. Check Jenkins Plugin Settings.)");
    }


    private void displayConnectionErrorMsg() {
        jenkinsBrowserPanel.setErrorMsg(configuration.getServerUrl(), "(Unable to connect. Check Jenkins Plugin Settings.)");
    }


    private View getSelectedJenkinsView() {
        return jenkinsBrowserPanel.getSelectedJenkinsView();
    }


    public JenkinsRequestManager getJenkinsManager() {
        return jenkinsRequestManager;
    }


    public void loadLatestBuilds(boolean shouldDisplayResult) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new LoadLatestBuildsJob(shouldDisplayResult));
        executorService.shutdown();
    }


    private static void installActionGroupInPopupMenu(ActionGroup group,
                                                      JComponent component,
                                                      ActionManager actionManager) {
        if (actionManager == null) {
            return;
        }
        PopupHandler.installPopupHandler(component, group, "POPUP", actionManager);
    }


    private static void installActionGroupInToolBar(ActionGroup actionGroup,
                                                    JComponent component,
                                                    ActionManager actionManager, String toolBarName) {
        if (actionManager == null) {
            return;
        }

        JComponent actionToolbar = ActionManager.getInstance()
                .createActionToolbar(toolBarName, actionGroup, true).getComponent();
        component.add(actionToolbar, BorderLayout.CENTER);
    }


    private void initListeners() {
        jenkinsBrowserPanel.getViewCombo().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    loadSelectedView();
                }
            }
        });
    }


    public JenkinsBrowserPanel getJenkinsBrowserPanel() {
        return jenkinsBrowserPanel;
    }


    public BrowserPreferences getBrowserPreferences() {
        return configuration.getBrowserPreferences();
    }


    @Override
    public void dispose() {
        scheduledThreadPoolExecutor.shutdown();
    }


    public interface BuildStatusListener {

        void onBuildFailure(String jobName, Build build);

        public static BuildStatusListener NULL = new BuildStatusListener() {
            public void onBuildFailure(String jobName, Build build) {
            }
        };
    }


    public interface JobLoadListener {

        void afterLoadingJobs(BuildStatusAggregator buildStatusAggregator);

        JobLoadListener NULL = new JobLoadListener() {
            @Override
            public void afterLoadingJobs(BuildStatusAggregator buildStatusAggregator) {
            }
        };
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

            configuration.getBrowserPreferences().setLastSelectedView(selectedView.getName());

            jenkinsBrowserPanel.startWaiting();
            final List<Job> jobList = jenkinsRequestManager.loadJenkinsView(selectedView.getUrl());

            getBrowserPreferences().setLastSelectedView(selectedView.getName());

            jenkins.setJobs(jobList);
            final BuildStatusAggregator buildStatusAggregator = new BuildStatusAggregator();

            GuiUtil.runInSwingThread(new Runnable() {
                @Override
                public void run() {
                    jenkinsBrowserPanel.fillJobTree(jenkins, buildStatusAggregator);
                    jenkinsBrowserPanel.endWaiting();
                    buildStatusAggregator.setNbJobs(jobList.size());
                    jobLoadListener.afterLoadingJobs(buildStatusAggregator);
                }

            });
        }
    }

    private class LoadLatestBuildsJob implements Runnable {
        private final boolean shouldDisplayResult;

        public LoadLatestBuildsJob(boolean shouldDisplayResult) {
            this.shouldDisplayResult = shouldDisplayResult;
        }

        @Override
        public void run() {
            final Map<String, Build> finishedBuilds = loadAndReturnNewLatestBuilds();
            if (!shouldDisplayResult) {
                return;
            }

            GuiUtil.runInSwingThread(new Runnable() {
                @Override
                public void run() {
                    rssLatestJobPanel.addFinishedBuild(finishedBuilds);

                    Entry<String, Build> firstFailedBuild = getFirstFailedBuild(finishedBuilds);
                    if (firstFailedBuild != null) {
                        buildStatusListener.onBuildFailure(firstFailedBuild.getKey(), firstFailedBuild.getValue());
                    }
                }
            });

        }
    }
}
