package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.JenkinsControlIcons;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobType;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

public class LoadBuildsAction extends AnAction implements DumbAware {

    public static final String ACTION_ID = "Jenkins.LoadBuilds";

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        loadBuilds(ActionUtil.getProject(event), ActionUtil.getBrowserPanel(event).getSelectedJob());
    }

    public void loadBuilds(Project project, Job job) {
        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        try {
            new Task.Backgroundable(project, getTemplatePresentation().getText(), false) {

                @Override
                public void onSuccess() {
                    super.onSuccess();
                    browserPanel.refreshJob(job);
                }

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    progressIndicator.setIndeterminate(true);
                    RequestManager requestManager = browserPanel.getJenkinsManager();
                    job.setLastBuilds(requestManager.loadBuilds(job));
                }
            }.queue();
        } catch (Exception ex) {
            browserPanel.notifyErrorJenkinsToolWindow("Unable to load builds: " + ex.getMessage());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Job selectedJob = ActionUtil.getBrowserPanel(event).getSelectedJob();
        event.getPresentation().setEnabled(selectedJob != null && selectedJob.getJobType() == JobType.JOB);
    }
}
