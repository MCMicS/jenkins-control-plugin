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

package org.codinjutsu.tools.jenkins.logic;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import lombok.Value;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JobTracker;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RssLogic implements Disposable {

    private final Project project;
    private final JenkinsAppSettings jenkinsAppSettings;
    private final Map<String, Build> currentBuildMap = new HashMap<>();

    private final Runnable refreshRssBuildsJob;
    private final LoadLatestBuildsJob loadLatestBuildsJob;
    private ScheduledFuture<?> refreshRssBuildFutureTask;

    public RssLogic(final Project project) {
        this.project = project;
        this.jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        this.loadLatestBuildsJob = new LoadLatestBuildsJob(project);
        this.refreshRssBuildsJob = loadLatestBuildsJob::queue;
    }

    public static RssLogic getInstance(Project project) {
        return project.getService(RssLogic.class);
    }

    public void loadLatestBuilds() {
        if (jenkinsAppSettings.isServerUrlSet()) {
            loadLatestBuildsJob.queue();
        }
    }

    public void initScheduledJobs() {
        final ExecutorService executorProvider = ExecutorService.getInstance(project);
        ScheduledThreadPoolExecutor executor = executorProvider.getExecutor();

        executorProvider.safeTaskCancel(refreshRssBuildFutureTask);

        executor.remove(refreshRssBuildsJob);

        if (jenkinsAppSettings.isServerUrlSet() && jenkinsAppSettings.getRssRefreshPeriod() > 0) {
            refreshRssBuildFutureTask = executor.scheduleWithFixedDelay(refreshRssBuildsJob, 0,
                    jenkinsAppSettings.getRssRefreshPeriod(), TimeUnit.MINUTES);
        }
    }

    @SuppressWarnings({"java:S3824", "java:S3398"})
    private Map<String, Build> loadAndReturnNewLatestBuilds(RequestManagerInterface requestManager) {
        final Map<String, Build> latestBuildMap = requestManager.loadJenkinsRssLatestBuilds(jenkinsAppSettings);
        final Map<String, Build> newBuildMap = new HashMap<>();
        for (Map.Entry<String, Build> entry : latestBuildMap.entrySet()) {
            String jobName = entry.getKey();
            Build newBuild = entry.getValue();
            Build currentBuild = currentBuildMap.get(jobName);

            if (!jenkinsAppSettings.shouldDisplayOnLogEvent(newBuild)) {
                continue;
            }

            if (!currentBuildMap.containsKey(jobName) || newBuild.isAfter(currentBuild)) {
                currentBuildMap.put(jobName, newBuild);
                newBuildMap.put(jobName, newBuild);
            }
        }

        return newBuildMap;
    }

    @SuppressWarnings("java:S3398")
    private void sendNotificationForEachBuild(List<Build> buildToSortByDateDescending) {
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
            final RssNotification rssNotification = buildMessage(build);
            final Notification notification = NotificationGroupManager.getInstance().getNotificationGroup("Jenkins Rss")
                    .createNotification("", rssNotification.getMessage(), notificationType, NotificationListener.URL_OPENING_LISTENER);
            Optional.ofNullable(rssNotification.getUrlToOpen())
                    .map(url -> NotificationAction.createSimple("Open in browser", () -> BrowserUtil.browse(url)))
                    .ifPresent(notification::addAction);
            notification.notify(project);
        }
    }

    @SuppressWarnings("java:S3398")
    private List<Build> sortByDateDescending(Map<String, Build> finishedBuilds) {
        final List<Build> buildToSortByDateDescending = new ArrayList<>(finishedBuilds.values());

        buildToSortByDateDescending.sort(Comparator.comparing(Build::getBuildDate));
        return buildToSortByDateDescending;
    }

    @SuppressWarnings("java:S3398")
    private void notifyFirstFailedBuild(Map.Entry<String, Build> firstFailedBuild) {
        if (firstFailedBuild != null) {
            final String jobName = firstFailedBuild.getKey();
            final Build build = firstFailedBuild.getValue();
            final String message = Optional.ofNullable(build.getFullDisplayName())
                    .orElseGet(() -> jobName + build.getDisplayNumber()) + ": FAILED";
            JenkinsNotifier.getInstance(project).notify(message, NotificationType.WARNING);
        }
    }

    @SuppressWarnings("java:S3398")
    @Nullable
    private Map.Entry<String, Build> getFirstFailedBuild(Map<String, Build> finishedBuilds) {
        for (Map.Entry<String, Build> buildByJobName : finishedBuilds.entrySet()) {
            Build build = buildByJobName.getValue();
            if (build.getStatus() == BuildStatusEnum.FAILURE) {
                return buildByJobName;
            }
        }
        return null;
    }

    private RssNotification buildMessage(Build build) {
        final BuildStatusEnum buildStatus = build.getStatus();
        final String messageWithPrefix = String.format("[Jenkins] %s", build.getMessage());
        if (buildStatus != BuildStatusEnum.SUCCESS && buildStatus != BuildStatusEnum.STABLE) {
            return new RssNotification(messageWithPrefix, build.getUrl());
        }
        return new RssNotification(messageWithPrefix, null);
    }

    @Override
    public void dispose() {
        currentBuildMap.clear();
    }

    @Value
    private static class RssNotification {
        private String message;
        private @Nullable String urlToOpen;
    }

    private class LoadLatestBuildsJob implements JenkinsBackgroundTask.JenkinsTask {

        @NotNull
        private final JenkinsBackgroundTask task;

        public LoadLatestBuildsJob(Project project) {
            this.task = JenkinsBackgroundTaskFactory.getInstance(project).createBackgroundTask(
                    "Load last builds", true, LoadLatestBuildsJob.this);
        }

        @Override
        public void run(@NotNull RequestManagerInterface requestManager) {
            final Map<String, Build> finishedBuilds = loadAndReturnNewLatestBuilds(requestManager);
            if (finishedBuilds.isEmpty()) {
                return;
            }

            JobTracker.getInstance().onNewFinishedBuilds(finishedBuilds);
            sendNotificationForEachBuild(sortByDateDescending(finishedBuilds));
            notifyFirstFailedBuild(getFirstFailedBuild(finishedBuilds));
        }

        public void queue() {
            task.queue();
        }
    }
}
