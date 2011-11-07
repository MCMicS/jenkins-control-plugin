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

package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.PopupHandler;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserPanel;
import org.codinjutsu.tools.jenkins.view.action.*;
import org.jdom.JDOMException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;

public class JenkinsBrowserLogic {

    private static final Logger LOG = Logger.getLogger(JenkinsBrowserLogic.class);

    private static final int MILLISECONDS = 1000;
    private static final int MINUTES = 60 * MILLISECONDS;
    private static final String JENKINS_JOB_ACTION_GROUP = "JenkinsJobGroup";
    private static final String JENKINS_RSS_ACTIONS = "JenkinsRssActions";
    private static final String JENKINS_ACTIONS = "jenkinsBrowserActions";

    private final JenkinsBrowserPanel browserPanel;
    private final JenkinsConfiguration configuration;
    private final JenkinsRequestManager jenkinsRequestManager;

    private Jenkins jenkins;
    private final Map<String, Build> currentBuildMap = new HashMap<String, Build>();
    private Timer jobRefreshTimer;
    private Timer rssRefreshTimer;

    public JenkinsBrowserLogic(JenkinsConfiguration configuration, JenkinsRequestManager jenkinsRequestManager) {
        this.configuration = configuration;
        this.jenkinsRequestManager = jenkinsRequestManager;
        this.browserPanel = new JenkinsBrowserPanel();
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


    public void init() {
        initView();
        reloadConfiguration();
    }


    private void initView() {

        installRssActions(getBrowserPanel().getRssActionPanel());
        installBrowserActions(getBrowserPanel().getJobTree(), getBrowserPanel().getActionPanel());

        initListeners();
    }


    public void reloadConfiguration() {
        loadJenkinsWorkspace();
        initTimers();
    }


    public void loadSelectedView() throws Exception {
        View jenkinsView = getSelectedJenkinsView();
        if (jenkinsView != null) {
            List<Job> jobList = jenkinsRequestManager.loadJenkinsView(jenkinsView.getUrl());
            jenkins.setJobs(jobList);
            this.browserPanel.fillJobTree(jenkins);
        } else {
            loadJenkinsWorkspace();
        }
    }


    public void loadSelectedJob() throws Exception {
        Job job = getSelectedJob();
        Job updatedJob = jenkinsRequestManager.loadJob(job.getUrl());
        job.updateContentWith(updatedJob);
    }


    public void refreshLatestCompletedBuilds() {
        try {
            if (jenkins != null && !jenkins.getJobs().isEmpty()) {
                Map<String, Build> latestBuild = jenkinsRequestManager.loadJenkinsRssLatestBuilds(
                        configuration);
                displayFinishedBuilds(addLatestBuilds(latestBuild));
            }
        } catch (JDOMException domEx) {
            String errorMessage = buildServerErrorMessage(domEx);
            LOG.error(errorMessage, domEx);
            showErrorDialog(errorMessage, "Error during parsing Rss Data");
        } catch (IOException ioEx) {
            LOG.error(buildServerErrorMessage(ioEx), ioEx);
            ioEx.printStackTrace();
        }
    }


    public void cleanRssEntries() {
        getBrowserPanel().getRssLatestJobPanel().cleanRssEntries();
    }


    public Job getSelectedJob() {
        return browserPanel.getSelectedJob();
    }


    public JenkinsBrowserPanel getBrowserPanel() {
        return browserPanel;
    }


    public Jenkins getJenkins() {
        return getBrowserPanel().getJenkins();
    }


    public JenkinsRequestManager getJenkinsManager() {
        return jenkinsRequestManager;
    }


    Map<String, Build> addLatestBuilds(Map<String, Build> latestBuildMap) {
        Map<String, Build> newBuildMap = new HashMap<String, Build>();
        for (Entry<String, Build> entry : latestBuildMap.entrySet()) {
            String jobName = entry.getKey();
            Build newBuild = entry.getValue();
            Build currentBuild = currentBuildMap.get(jobName);
            if (!currentBuildMap.containsKey(jobName) || newBuild.isDisplayable(currentBuild)) {
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
                jenkinsRequestManager.authenticate(configuration.getServerUrl(), configuration.getSecurityMode(), configuration.getUsername(), configuration.getPassword());
                jenkins = jenkinsRequestManager.loadJenkinsWorkspace(configuration);
                browserPanel.initModel(jenkins);
                String preferredView = configuration.getPreferredView();
                View jenkinsView = findView(preferredView);
                if (jenkinsView != null) {
                    this.browserPanel.setSelectedView(jenkinsView);
                } else {
                    this.browserPanel.setSelectedView(jenkins.getPrimaryView());
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
        if (jobRefreshTimer != null) {
            jobRefreshTimer.cancel();
        }

        if (rssRefreshTimer != null) {
            rssRefreshTimer.cancel();
        }

        if (configuration.isEnableJobAutoRefresh()) {
            jobRefreshTimer = new Timer();
            jobRefreshTimer.schedule(new JobRefreshTimerTask(),
                    MINUTES,
                    configuration.getJobRefreshPeriod() * MINUTES);
        }

        if (configuration.isEnableRssAutoRefresh()) {
            rssRefreshTimer = new Timer();
            rssRefreshTimer.schedule(new RssRefreshTimerTask(),
                    MINUTES,
                    configuration.getRssRefreshPeriod() * MINUTES);
        }
    }


    private View findView(String preferredView) {
        List<View> viewList = jenkins.getViews();
        for (View jenkinsView : viewList) {
            String viewName = jenkinsView.getName();
            if (viewName.equals(preferredView)) {
                return jenkinsView;
            }
        }
        return jenkins.getPrimaryView();
    }


    private void displayConnectionErrorMsg() {
        getBrowserPanel().setErrorMsg();
    }


    private void displayFinishedBuilds(Map<String, Build> displayableFinishedBuilds) {
        getBrowserPanel().getRssLatestJobPanel().addFinishedBuild(displayableFinishedBuilds);
    }


    private void showErrorDialog(String errorMessage, String title) {
        getBrowserPanel().showErrorDialog(errorMessage, title);
    }


    private View getSelectedJenkinsView() {
        return browserPanel.getSelectedJenkinsView();
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
            actionGroup.add(new GotoJobPageAction(getBrowserPanel()));
            actionGroup.add(new GotoLastBuildPageAction(getBrowserPanel()));
            actionGroup.addSeparator();
            actionGroup.add(new OpenPluginSettingsAction());
        }
        installActionGroupInToolBar(actionGroup, toolBar, ActionManager.getInstance(), JENKINS_ACTIONS);
        installActionGroupInPopupMenu(actionGroup, jobTree, ActionManager.getInstance());
    }


    private void initListeners() {
        getBrowserPanel().getViewCombo().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    try {
                        loadSelectedView();
                    } catch (Exception e) {
//TODO Gros KK !
                    }
                }
            }
        });
    }


    private class JobRefreshTimerTask extends TimerTask {

        @Override
        public void run() {
            try {
                loadSelectedView();
            } catch (Exception e) {
//TODO Gros KK !
            }
        }
    }


    private class RssRefreshTimerTask extends TimerTask {

        @Override
        public void run() {
            refreshLatestCompletedBuilds();
        }
    }
}
