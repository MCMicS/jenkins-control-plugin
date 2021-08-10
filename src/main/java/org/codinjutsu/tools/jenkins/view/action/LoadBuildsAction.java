package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.logic.JenkinsBackgroundTask;
import org.codinjutsu.tools.jenkins.logic.JenkinsBackgroundTaskFactory;
import org.codinjutsu.tools.jenkins.logic.RequestManagerInterface;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobType;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LoadBuildsAction extends AnAction implements DumbAware, UpdateInBackground {

    public static final String ACTION_ID = "Jenkins.LoadBuilds";

    public static boolean isAvailable(@Nullable Job job) {
        return job != null && job.getJobType() == JobType.JOB;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ActionUtil.getProject(event).ifPresent(this::loadBuildsForSelectedJob);
    }

    private void loadBuildsForSelectedJob(Project project) {
        Optional.ofNullable(BrowserPanel.getInstance(project).getSelectedJob()).ifPresent(job -> loadBuilds(project, job));
    }

    public void loadBuilds(Project project, Job job) {
        final boolean expandAfterLoad = job.getLastBuilds().isEmpty();
        JenkinsBackgroundTaskFactory.getInstance(project).createBackgroundTask(getTemplatePresentation().getText(),
                new JenkinsBackgroundTask.JenkinsTask() {

                    @Override
                    public void onSuccess() {
                        JenkinsBackgroundTask.JenkinsTask.super.onSuccess();
                        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
                        browserPanel.refreshJob(job);
                        if (expandAfterLoad) {
                            browserPanel.expandSelectedJob();
                        }
                    }

                    @Override
                    public void run(@NotNull RequestManagerInterface requestManager) {
                        job.setLastBuilds(requestManager.loadBuilds(job));
                    }
                }).queue();
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        boolean isAvailable = ActionUtil.getBrowserPanel(event).map(BrowserPanel::getSelectedJob)
                .map(LoadBuildsAction::isAvailable)
                .orElse(Boolean.FALSE);
        event.getPresentation().setEnabled(isAvailable);
    }
}
