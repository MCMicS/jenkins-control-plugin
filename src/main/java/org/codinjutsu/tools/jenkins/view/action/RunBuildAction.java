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

package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.ExecutorService;
import org.codinjutsu.tools.jenkins.logic.JenkinsBackgroundTask;
import org.codinjutsu.tools.jenkins.logic.JenkinsBackgroundTaskFactory;
import org.codinjutsu.tools.jenkins.logic.RequestManagerInterface;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildType;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.BuildParamDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.codinjutsu.tools.jenkins.view.BrowserPanel.POPUP_PLACE;

public class RunBuildAction extends AnAction implements DumbAware, UpdateInBackground {

    public static final String ACTION_ID = "Jenkins.RunBuild";
    public static final int BUILD_STATUS_UPDATE_DELAY = 1;
    private static final Logger LOG = Logger.getInstance(RunBuildAction.class.getName());
    private static final Consumer<Job> DO_NOTHING = job -> {
    };

    public static boolean isBuildable(@Nullable Job job) {
        return job != null && job.isBuildable();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ActionUtil.getProject(event).ifPresent(this::actionPerformed);
    }

    private void actionPerformed(@NotNull Project project) {
        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        try {
            Optional.ofNullable(browserPanel.getSelectedJob())
                    .ifPresent(job -> queueRunBuild(project, browserPanel, job));
        } catch (Exception ex) {
            final String message = ex.getMessage() == null ? "Unknown error" : ex.getMessage();
            LOG.error(message, ex);
            browserPanel.notifyErrorJenkinsToolWindow("Build cannot be run: " + message);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final boolean isBuildable = ActionUtil.getBrowserPanel(event).map(BrowserPanel::getSelectedJob)
                .map(RunBuildAction::isBuildable).orElse(Boolean.FALSE);
        if (event.getPlace().equals(POPUP_PLACE)) {
            event.getPresentation().setVisible(isBuildable);
        } else {
            event.getPresentation().setEnabled(isBuildable);
        }
    }

    private void notifyOnGoingMessage(BrowserPanel browserPanel, Job job) {
        browserPanel.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                job.getNameToRenderSingleJob() + " build is on going",
                job.getUrl()));
    }

    private void queueRunBuild(@NotNull Project project, @NotNull BrowserPanel browserPanel, @NotNull Job job) {
        final Optional<Build> previousLastBuild = Optional.ofNullable(job.getLastBuild());
        JenkinsBackgroundTaskFactory.getInstance(project).createBackgroundTask("Running build",
                new JenkinsBackgroundTask.JenkinsTask() {

                    @Override
                    public void run(@NotNull RequestManagerInterface requestManager) {
                        if (job.hasParameters()) {
                            GuiUtil.runInSwingThread(() -> browserPanel.loadJob(job,//
                                    reloadedJob -> showRunDialog(project, reloadedJob, browserPanel)));
                        } else {
                            requestManager.runBuild(job, JenkinsAppSettings.getSafeInstance(project),
                                    Collections.emptyMap());
                            notifyOnGoingMessage(browserPanel, job);
                        }
                    }

                    @Override
                    public void onSuccess() {
                        JenkinsBackgroundTask.JenkinsTask.super.onSuccess();
                        ExecutorService.getInstance(project).getExecutor().schedule(() -> GuiUtil.runInSwingThread(() -> {
                            final Optional<Job> newJob = browserPanel.getJob(job.getNameToRenderSingleJob());
                            final Consumer<Job> openLogAfterReload;
                            if (JenkinsAppSettings.getSafeInstance(project).isShowLogIfTriggerBuild()) {
                                openLogAfterReload = loaded -> openLogIfRunning(loaded, previousLastBuild.orElse(null));
                            } else {
                                openLogAfterReload = DO_NOTHING;
                            }
                            newJob.ifPresent(j -> browserPanel.loadJob(j, openLogAfterReload));
                        }), BUILD_STATUS_UPDATE_DELAY, TimeUnit.SECONDS);
                    }

                    private void openLogIfRunning(Job jobToCheckIfIsNewer, @Nullable Build previousLastBuild) {
                        final Build lastBuild = jobToCheckIfIsNewer.getLastBuild();
                        final boolean isNewerBuild = lastBuild != null &&
                                (previousLastBuild == null || previousLastBuild.getNumber() < lastBuild.getNumber());
                        if (isNewerBuild && lastBuild.isBuilding()) {
                            final LogToolWindow logToolWindow = new LogToolWindow(project);
                            logToolWindow.showLog(BuildType.LAST, job, browserPanel);
                        }
                    }
                }).queue();
    }

    private void showRunDialog(@NotNull Project project, @NotNull Job job, @NotNull BrowserPanel browserPanel) {
        RequestManagerInterface requestManager = browserPanel.getJenkinsManager();
        BuildParamDialog.showDialog(project, job, JenkinsAppSettings.getSafeInstance(project),
                requestManager, new BuildParamDialog.RunBuildCallback() {

                    public void notifyOnOk(Job job) {
                        notifyOnGoingMessage(browserPanel, job);
                        browserPanel.loadJob(job);
                    }

                    public void notifyOnError(Job job, Throwable ex) {
                        browserPanel.notifyErrorJenkinsToolWindow("Build '" + job.getNameToRenderSingleJob() + "' cannot be run: " + ex.getMessage());
                        browserPanel.loadJob(job);
                    }

                });
    }
}
