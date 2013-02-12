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

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;

import java.util.*;
import java.util.concurrent.*;

public class RssLogic {

    private final NotificationGroup JENKINS_RSS_GROUP = NotificationGroup.logOnlyGroup("Jenkins Rss");

    private final BuildStatusListener buildStatusListener;

    private final Project project;
    private final JenkinsAppSettings jenkinsAppSettings;
    private RequestManager requestManager;

    private final Map<String, Build> currentBuildMap = new HashMap<String, Build>();

    private final Runnable refreshRssBuildsJob = new LoadLatestBuildsJob(true);
    private ScheduledFuture<?> refreshRssBuildFutureTask;

    public RssLogic(Project project, JenkinsAppSettings jenkinsAppSettings, RequestManager requestManager, BuildStatusListener buildStatusListener) {
        this.project = project;
        this.jenkinsAppSettings = jenkinsAppSettings;
        this.requestManager = requestManager;
        this.buildStatusListener = buildStatusListener;
    }

    public void loadLatestBuilds(boolean shouldDisplayResult) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new LoadLatestBuildsJob(shouldDisplayResult));
        executorService.shutdown();
    }


    void initScheduledJobs(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
        safeTaskCancel(refreshRssBuildFutureTask);

        scheduledThreadPoolExecutor.remove(refreshRssBuildsJob);

        if (jenkinsAppSettings.getRssRefreshPeriod() > 0) {
            refreshRssBuildFutureTask = scheduledThreadPoolExecutor.scheduleWithFixedDelay(refreshRssBuildsJob, jenkinsAppSettings.getRssRefreshPeriod(), jenkinsAppSettings.getRssRefreshPeriod(), TimeUnit.MINUTES);
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
        Map<String, Build> latestBuildMap = requestManager.loadJenkinsRssLatestBuilds(jenkinsAppSettings);
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
        loadLatestBuilds(false);
    }

    private class LoadLatestBuildsJob implements Runnable {
        private final boolean shouldDisplayResult;

        public LoadLatestBuildsJob(boolean shouldDisplayResult) {
            this.shouldDisplayResult = shouldDisplayResult;
        }

        @Override
        public void run() {
            final Map<String, Build> finishedBuilds = loadAndReturnNewLatestBuilds();
            if (!shouldDisplayResult || finishedBuilds.isEmpty()) {
                return;
            }


            final ArrayList<Build> buildToSortByDateDescending = new ArrayList<Build>(finishedBuilds.values());

            Collections.sort(buildToSortByDateDescending, new Comparator<Build>() {
                @Override
                public int compare(Build firstBuild, Build secondBuild) {
                    return firstBuild.getBuildDate().compareTo(secondBuild.getBuildDate());
                }
            });

            for (Build build : buildToSortByDateDescending) {
                BuildStatusEnum status = build.getStatus();
                NotificationType notificationType;
                if (BuildStatusEnum.SUCCESS.equals(status) || BuildStatusEnum.STABLE.equals(status)) {
                    notificationType = NotificationType.INFORMATION;
                } else if (BuildStatusEnum.FAILURE.equals(status) || (BuildStatusEnum.UNSTABLE.equals(status))) {
                    notificationType = NotificationType.ERROR;
                } else {
                    notificationType = NotificationType.WARNING;
                }
                JENKINS_RSS_GROUP
                        .createNotification("", buildMessage(build), notificationType, NotificationListener.URL_OPENING_LISTENER)
                        .notify(project);
            }

            Map.Entry<String, Build> firstFailedBuild = getFirstFailedBuild(finishedBuilds);
            if (firstFailedBuild != null) {
                buildStatusListener.onBuildFailure(firstFailedBuild.getKey(), firstFailedBuild.getValue());
            }
        }

    }

    public static String buildMessage(Build build) {
        BuildStatusEnum buildStatus = build.getStatus();
        String buildMessage = build.getMessage();

        if (buildStatus != BuildStatusEnum.SUCCESS && buildStatus != BuildStatusEnum.STABLE) {
            return String.format("<html><body>[Jenkins] <a href='%s'>%s</a><body></html>", build.getUrl(), buildMessage);
        }
        return String.format("[Jenkins] %s", buildMessage);
    }


    public interface BuildStatusListener {

        void onBuildFailure(String jobName, Build build);

        public static BuildStatusListener NULL = new BuildStatusListener() {
            public void onBuildFailure(String jobName, Build build) {
            }
        };
    }
}
