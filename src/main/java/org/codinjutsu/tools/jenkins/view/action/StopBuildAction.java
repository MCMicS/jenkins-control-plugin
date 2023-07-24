package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import lombok.Value;
import org.codinjutsu.tools.jenkins.logic.JenkinsBackgroundTask;
import org.codinjutsu.tools.jenkins.logic.JenkinsBackgroundTaskFactory;
import org.codinjutsu.tools.jenkins.logic.RequestManagerInterface;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Optional;

import static org.codinjutsu.tools.jenkins.view.BrowserPanel.POPUP_PLACE;

public class StopBuildAction extends AnAction implements DumbAware {
    public static final String ACTION_ID = "Jenkins.StopBuild";

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ActionUtil.getProject(event).ifPresent(this::actionPerformed);
    }

    private void actionPerformed(@NotNull Project project) {
        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        final LastSelection lastSelection = calculateLastSelection(browserPanel);
        Optional.ofNullable(lastSelection.getBuild()).ifPresent(build -> stopBuild(project, browserPanel, build,
                lastSelection.getJob()));
    }

    private void stopBuild(@NotNull Project project, @NotNull BrowserPanel browserPanel, Build build,
                           @Nullable Job job) {
        JenkinsBackgroundTaskFactory.getInstance(project).createBackgroundTask("Stopping build",
                new JenkinsBackgroundTask.JenkinsTask() {
                    @Override
                    public void run(@NotNull RequestManagerInterface requestManager) {
                        requestManager.stopBuild(build);
                    }

                    @Override
                    public void onSuccess() {
                        JenkinsBackgroundTask.JenkinsTask.super.onSuccess();
                        if (job == null) {
                            browserPanel.refreshCurrentView();
                        } else {
                            browserPanel.loadJob(job);
                        }
                    }
                }).queue();
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        final var lastSelection = ActionUtil.getBrowserPanel(event).map(StopBuildAction::calculateLastSelection)
                .orElse(LastSelection.NO_SELECTION);
        update(event, lastSelection);
    }

    private void update(AnActionEvent event, LastSelection lastSelection) {
        final Optional<Job> job = Optional.ofNullable(lastSelection.getJob());
        final boolean isInQueue = job.map(Job::isInQueue).orElse(Boolean.FALSE);
        final boolean isBuilding = Optional.ofNullable(lastSelection.getBuild())
                .map(Build::isBuilding).orElse(Boolean.FALSE);
        final BuildStatusEnum lastBuildStatus = Optional.ofNullable(lastSelection.getBuild())
                .map(Build::getStatus)
                .orElse(BuildStatusEnum.NULL);
        final boolean isStoppable = lastBuildStatus == BuildStatusEnum.RUNNING || isBuilding || isInQueue;
        if (event.getPlace().equals(POPUP_PLACE)) {
            event.getPresentation().setVisible(isStoppable);
        } else {
            event.getPresentation().setEnabled(isStoppable);
        }
    }

    @Nonnull
    private static LastSelection calculateLastSelection(@CheckForNull BrowserPanel browserPanel) {
        final Optional<Job> job = Optional.ofNullable(browserPanel).map(BrowserPanel::getSelectedJob);
        final Optional<Build> build = Optional.ofNullable(browserPanel).flatMap(BrowserPanel::getSelectedBuild)
                .or(() -> job.map(Job::getLastBuild));
        // TODO find job for selected Build
        return new LastSelection(job.orElse(null), build.orElse(null));
    }

    @Value
    private static class LastSelection {

        public static final LastSelection NO_SELECTION = new LastSelection(null, null);

        @Nullable
        private Job job;
        @Nullable
        private Build build;
    }
}
