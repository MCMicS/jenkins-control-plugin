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

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.JenkinsWidget;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RssLogic implements Disposable {

    private final NotificationGroup JENKINS_RSS_GROUP = NotificationGroup.logOnlyGroup("Jenkins Rss");

    private final Project project;
    private final JenkinsAppSettings jenkinsAppSettings;
    private RequestManager requestManager;

    private Map<String, Build> currentBuildMap = new HashMap<String, Build>();

    private final Runnable refreshRssBuildsJob;
    private ScheduledFuture<?> refreshRssBuildFutureTask;

    public static RssLogic getInstance(Project project) {
        return ServiceManager.getService(project, RssLogic.class);
    }

    public RssLogic(final Project project) {
        this.project = project;
        this.jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        this.requestManager = RequestManager.getInstance(project);
        refreshRssBuildsJob = new Runnable() {
            @Override
            public void run() {
                GuiUtil.runInSwingThread(new Runnable() {
                    @Override
                    public void run() {
                        new LoadLatestBuildsJob(project, true).queue();
                    }
                });
            }
        };
    }

    public void loadLatestBuilds(boolean shouldDisplayResult) {
        if (jenkinsAppSettings.isServerUrlSet()) {
            new LoadLatestBuildsJob(project, shouldDisplayResult).queue();
        }
    }


    public void initScheduledJobs(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor1) {
        safeTaskCancel(refreshRssBuildFutureTask);

        scheduledThreadPoolExecutor1.remove(refreshRssBuildsJob);

        if (jenkinsAppSettings.getRssRefreshPeriod() > 0) {
            refreshRssBuildFutureTask = scheduledThreadPoolExecutor1.scheduleWithFixedDelay(refreshRssBuildsJob, jenkinsAppSettings.getRssRefreshPeriod(), jenkinsAppSettings.getRssRefreshPeriod(), TimeUnit.MINUTES);
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

    public void init() {
        loadLatestBuilds(false);
    }

    @Override
    public void dispose() {
        currentBuildMap = null;
    }


    private class LoadLatestBuildsJob extends Task.Backgroundable {
        private final boolean shouldDisplayResult;

        public LoadLatestBuildsJob(Project project, boolean shouldDisplayResult) {
            super(project, "Load last builds", true, JenkinsLoadingTaskOption.INSTANCE);
            this.shouldDisplayResult = shouldDisplayResult;
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
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
                String jobName = firstFailedBuild.getKey();
                Build build = firstFailedBuild.getValue();
                BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(jobName + "#" + build.getNumber() + ": FAILED", MessageType.ERROR, null);
                final Balloon balloon = balloonBuilder.setFadeoutTime(TimeUnit.SECONDS.toMillis(1)).createBalloon();
                GuiUtil.runInSwingThread(new Runnable() {
                    @Override
                    public void run() {
                        balloon.show(new RelativePoint(JenkinsWidget.getInstance(project).getComponent(), new Point(0, 0)), Balloon.Position.above);
                    }
                });
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
}
