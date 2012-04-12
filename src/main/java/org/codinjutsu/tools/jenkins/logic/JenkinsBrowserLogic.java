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

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.PopupHandler;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserPanel;
import org.codinjutsu.tools.jenkins.view.JobSearchComponent;
import org.codinjutsu.tools.jenkins.view.RssLatestBuildPanel;
import org.codinjutsu.tools.jenkins.view.action.*;
import org.codinjutsu.tools.jenkins.view.action.search.NextOccurrenceAction;
import org.codinjutsu.tools.jenkins.view.action.search.OpenJobSearchPanelAction;
import org.codinjutsu.tools.jenkins.view.action.search.PrevOccurrenceAction;
import org.jdom.JDOMException;

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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JenkinsBrowserLogic {

    private static final Logger LOG = Logger.getLogger(JenkinsBrowserLogic.class);

    private static final String JENKINS_JOB_ACTION_GROUP = "JenkinsJobGroup";
    private static final String JENKINS_RSS_ACTIONS = "JenkinsRssActions";
    private static final String JENKINS_ACTIONS = "jenkinsBrowserActions";

    private final JenkinsConfiguration configuration;
    private final JenkinsRequestManager jenkinsRequestManager;

    private final RssBuildStatusCallback rssBuildStatusCallback;

    private Jenkins jenkins;
    private final Map<String, Build> currentBuildMap = new HashMap<String, Build>();
    private final JenkinsBrowserPanel jenkinsBrowserPanel;
    private final RssLatestBuildPanel rssLatestJobPanel;

    private JobViewCallback jobViewCallback = JobViewCallback.NULL;


    public JenkinsBrowserLogic(JenkinsConfiguration configuration, JenkinsRequestManager jenkinsRequestManager, JenkinsBrowserPanel jenkinsBrowserPanel, RssLatestBuildPanel rssLatestJobPanel, RssBuildStatusCallback rssBuildStatusCallback, JobViewCallback jobViewCallback) {
        this.configuration = configuration;
        this.jenkinsRequestManager = jenkinsRequestManager;
        this.jenkinsBrowserPanel = jenkinsBrowserPanel;
        this.rssLatestJobPanel = rssLatestJobPanel;
        this.rssBuildStatusCallback = rssBuildStatusCallback;
        this.jobViewCallback = jobViewCallback;
    }


    public void init() {
        initView();
        reloadConfiguration();
    }


    private void initView() {
        jenkinsBrowserPanel.createSearchPanel();
        installRssActions(rssLatestJobPanel.getRssActionPanel());
        installBrowserActions(jenkinsBrowserPanel.getJobTree(), jenkinsBrowserPanel.getActionPanel());
        installSearchActions(jenkinsBrowserPanel.getSearchComponent());
        initListeners();
    }


    public void reloadConfiguration() { //TODO see how to centralize thread creation
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                loadJenkinsWorkspace();
                loadAnReturnNewLatestBuilds();
            }
        });

        executor.shutdown();

        cleanRssEntries();

        initTimers();
    }


    public void loadSelectedView() throws Exception {
        View jenkinsView = getSelectedJenkinsView();
        if (jenkinsView != null) {
            List<Job> jobList = jenkinsRequestManager.loadJenkinsView(jenkinsView.getUrl());
            jenkins.setJobs(jobList);
            this.jenkinsBrowserPanel.fillJobTree(jenkins);
        } else {
            loadJenkinsWorkspace();
        }

        jobViewCallback.doAfterLoadingJobs(jenkins);
    }


    public void loadSelectedJob() throws Exception {
        Job job = getSelectedJob();
        Job updatedJob = jenkinsRequestManager.loadJob(job.getUrl());
        job.updateContentWith(updatedJob);
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


    public void cleanRssEntries() {
        rssLatestJobPanel.cleanRssEntries();
    }


    public Job getSelectedJob() {
        return jenkinsBrowserPanel.getSelectedJob();
    }

    public Jenkins getJenkins() {
        return jenkinsBrowserPanel.getJenkins();
    }


    public JenkinsRequestManager getJenkinsManager() {
        return jenkinsRequestManager;
    }


    private Map<String, Build> loadAnReturnNewLatestBuilds() {
        Map<String, Build> latestBuildMap;
        try {
            latestBuildMap = jenkinsRequestManager.loadJenkinsRssLatestBuilds(configuration);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            displayConnectionErrorMsg();
            return new HashMap<String, Build>();
        }

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


    String buildServerErrorMessage(Exception ex) {
        return "Server Url=" + configuration.getServerUrl() + "\n" + ex.getMessage();
    }


    private void loadJenkinsWorkspace() {
        if (configuration.isServerUrlSet()) {
            try {
                jenkinsRequestManager.authenticate(configuration.getServerUrl(), configuration.getSecurityMode(), configuration.getUsername(), configuration.getPasswordFile(), configuration.getCrumbFile());
                jenkins = jenkinsRequestManager.loadJenkinsWorkspace(configuration);
                jenkinsBrowserPanel.initModel(jenkins);
                String preferredView = configuration.getPreferredView();
                View jenkinsView = findView(preferredView);
                if (jenkinsView != null) {
                    this.jenkinsBrowserPanel.setSelectedView(jenkinsView);
                } else {
                    this.jenkinsBrowserPanel.setSelectedView(jenkins.getPrimaryView());
                }
            } catch (JDOMException domEx) {
                String errorMessage = buildServerErrorMessage(domEx);
                LOG.error(errorMessage, domEx);
                showErrorDialog(errorMessage, "Error during parsing workspace");
            } catch (Exception ex) {
                LOG.error(buildServerErrorMessage(ex), ex);
                displayConnectionErrorMsg();
            }
        } else {
            displayConnectionErrorMsg();
        }
    }


    private void initTimers() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

        if (configuration.isEnableJobAutoRefresh()) {
            executorService.scheduleAtFixedRate(new JobRefreshTask(this), 1, configuration.getJobRefreshPeriod(), TimeUnit.MINUTES);
        }

        if (configuration.isEnableRssAutoRefresh()) {
            executorService.scheduleAtFixedRate(new RssRefreshTask(this), 1, configuration.getRssRefreshPeriod(), TimeUnit.MINUTES);
        }
    }


    private View findView(String preferredView) {
        List<View> viewList = jenkins.getViews();
        for (View jenkinsView : viewList) {
            if (jenkinsView.hasNestedView()) {
                for (View subView : jenkinsView.getSubViews()) {
                    String subViewName = subView.getName();
                    if (subViewName.equals(preferredView)) {
                        return subView;
                    }
                }
            } else {
                String viewName = jenkinsView.getName();
                if (viewName.equals(preferredView)) {
                    return jenkinsView;
                }
            }

        }
        return null;
    }


    private void displayConnectionErrorMsg() {
        jenkinsBrowserPanel.setErrorMsg();
    }


    private void displayFinishedBuilds(Map<String, Build> finishedBuilds) {
        rssLatestJobPanel.addFinishedBuild(finishedBuilds);

        Entry<String, Build> firstFailedBuild = getFirstFailedBuild(finishedBuilds);
        if (firstFailedBuild != null) {
            rssBuildStatusCallback.notifyOnBuildFailure(firstFailedBuild.getKey(), firstFailedBuild.getValue());
        }

    }


    private void showErrorDialog(String errorMessage, String title) {
        jenkinsBrowserPanel.showErrorDialog(errorMessage, title);
    }


    private View getSelectedJenkinsView() {
        return jenkinsBrowserPanel.getSelectedJenkinsView();
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

    public RssLatestBuildPanel getRssLatestJobPanel() {
        return rssLatestJobPanel;
    }


    public JenkinsBrowserPanel getJenkinsBrowserPanel() {
        return jenkinsBrowserPanel;
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
                    try {
                        loadSelectedView();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                }
            }
        });
    }

    public void loadLatestBuilds() {
        Map<String, Build> finishedBuilds = loadAnReturnNewLatestBuilds();
        displayFinishedBuilds(finishedBuilds);
    }

    public interface RssBuildStatusCallback {

        void notifyOnBuildFailure(String jobName, Build build);

        public static RssBuildStatusCallback NULL = new RssBuildStatusCallback() {
            public void notifyOnBuildFailure(String jobName, Build build) {
            }
        };
    }

    public interface JobViewCallback {

        void doAfterLoadingJobs(Jenkins jenkins);

        JobViewCallback NULL = new JobViewCallback() {
            @Override
            public void doAfterLoadingJobs(Jenkins jenkins) {
            }
        };
    }
}
