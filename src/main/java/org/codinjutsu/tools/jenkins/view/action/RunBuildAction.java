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
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.ExecutorService;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.BuildParamDialog;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.codinjutsu.tools.jenkins.view.BrowserPanel.POPUP_PLACE;

public class RunBuildAction extends AnAction implements DumbAware {

    public static final String ACTION_ID = "Jenkins.RunBuild";

    private static final Logger LOG = Logger.getLogger(RunBuildAction.class.getName());
    public static final int BUILD_STATUS_UPDATE_DELAY = 1;

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final Project project = ActionUtil.getProject(event);
        final BrowserPanel browserPanel = ActionUtil.getBrowserPanel(event);
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
        final BrowserPanel browserPanel = ActionUtil.getBrowserPanel(event);
        Job selectedJob = browserPanel.getSelectedJob();

        final boolean isBuildable = selectedJob != null && selectedJob.isBuildable();
        if (event.getPlace().equals(POPUP_PLACE)) {
            event.getPresentation().setVisible(isBuildable);
        } else {
            event.getPresentation().setEnabled(isBuildable);
        }
    }

    private void notifyOnGoingMessage(BrowserPanel browserPanel, Job job) {
        browserPanel.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                job.getName() + " build is on going",
                job.getUrl()));
    }

    private void queueRunBuild(@NotNull Project project, @NotNull BrowserPanel browserPanel, @NotNull Job job) {
        new Task.Backgroundable(project, "Running build", false) {

            @Override
            public void onSuccess() {
                ExecutorService.getInstance(project).getExecutor().schedule(() -> GuiUtil.runInSwingThread(() -> {
                    final Optional<Job> newJob = browserPanel.getJob(job.getName());
                    newJob.ifPresent(browserPanel::loadJob);
                }), BUILD_STATUS_UPDATE_DELAY, TimeUnit.SECONDS);
            }

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);
                RequestManager requestManager = browserPanel.getJenkinsManager();
                if (job.hasParameters()) {
                    BuildParamDialog.showDialog(project, job, JenkinsAppSettings.getSafeInstance(project), requestManager, new BuildParamDialog.RunBuildCallback() {

                        public void notifyOnOk(Job job) {
                            notifyOnGoingMessage(browserPanel, job);
                            browserPanel.loadJob(job);
                        }

                        public void notifyOnError(Job job, Throwable ex) {
                            browserPanel.notifyErrorJenkinsToolWindow("Build '" + job.getName() + "' cannot be run: " + ex.getMessage());
                            browserPanel.loadJob(job);
                        }

                    });

                } else {
                    requestManager.runBuild(job, JenkinsAppSettings.getSafeInstance(project), Collections.emptyMap());
                    notifyOnGoingMessage(browserPanel, job);
                }
            }
        }.queue();
    }
}
