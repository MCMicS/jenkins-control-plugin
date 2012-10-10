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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.RssLatestBuildPanel;
import org.codinjutsu.tools.jenkins.view.action.CleanRssAction;
import org.codinjutsu.tools.jenkins.view.action.RefreshRssAction;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static org.codinjutsu.tools.jenkins.util.GuiUtil.installActionGroupInToolBar;

public class RssLogic {

    private static final String JENKINS_RSS_ACTIONS = "JenkinsRssActions";

    private final RssLatestBuildPanel rssLatestJobPanel;
    private final BuildStatusListener buildStatusListener;

    private final JenkinsConfiguration configuration;
    private RequestManager requestManager;

    private final Map<String, Build> currentBuildMap = new HashMap<String, Build>();

    private final Runnable refreshRssBuildsJob = new LoadLatestBuildsJob(true);
    private ScheduledFuture<?> refreshRssBuildFutureTask;

    public RssLogic(JenkinsConfiguration configuration, RequestManager requestManager, RssLatestBuildPanel rssLatestJobPanel, BuildStatusListener buildStatusListener) {
        this.configuration = configuration;
        this.requestManager = requestManager;
        this.rssLatestJobPanel = rssLatestJobPanel;
        this.buildStatusListener = buildStatusListener;
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

    public void loadLatestBuilds(boolean shouldDisplayResult) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new LoadLatestBuildsJob(shouldDisplayResult));
        executorService.shutdown();
    }


    public void cleanRssEntries() {
        rssLatestJobPanel.cleanRssEntries();
    }

    void initScheduledJobs(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        safeTaskCancel(refreshRssBuildFutureTask);

        scheduledThreadPoolExecutor.remove(refreshRssBuildsJob);

        if (configuration.isEnableRssAutoRefresh()) {
            refreshRssBuildFutureTask = scheduledThreadPoolExecutor.scheduleWithFixedDelay(refreshRssBuildsJob, configuration.getRssRefreshPeriod(), configuration.getRssRefreshPeriod(), TimeUnit.MINUTES);
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


    private Map.Entry<String, Build> getFirstFailedBuild(Map<String, Build> finishedBuilds) {
        for (Map.Entry<String, Build> buildByJobName : finishedBuilds.entrySet()) {
            Build build = buildByJobName.getValue();
            if (build.getStatus() == BuildStatusEnum.FAILURE) {
                return buildByJobName;
            }
        }
        return null;
    }

    private Map<String, Build> loadAndReturnNewLatestBuilds() {
        Map<String, Build> latestBuildMap = requestManager.loadJenkinsRssLatestBuilds(configuration);
        Map<String, Build> newBuildMap = new HashMap<String, Build>();
        for (Map.Entry<String, Build> entry : latestBuildMap.entrySet()) {
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

    public void init() {
        initGui();
        reloadConfiguration();

        loadLatestBuilds(false);
    }

    void reloadConfiguration() {
        cleanRssEntries();
    }

    private void initGui() {
        installRssActions(rssLatestJobPanel.getRssActionPanel());
    }

    public void dispose() {
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

                    Map.Entry<String, Build> firstFailedBuild = getFirstFailedBuild(finishedBuilds);
                    if (firstFailedBuild != null) {
                        buildStatusListener.onBuildFailure(firstFailedBuild.getKey(), firstFailedBuild.getValue());
                    }
                }
            });

        }
    }

    public interface BuildStatusListener {

        void onBuildFailure(String jobName, Build build);

        public static BuildStatusListener NULL = new BuildStatusListener() {
            public void onBuildFailure(String jobName, Build build) {
            }
        };
    }
}
