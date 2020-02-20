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
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

public class LoadBuildsAction extends AnAction implements DumbAware {

    private final BrowserPanel browserPanel;

    public LoadBuildsAction(BrowserPanel browserPanel) {
        super("Load builds", "Load builds", JenkinsControlIcons.LOAD_BUILDS);
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = ActionUtil.getProject(event);
        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);

        try {
            final Job job = browserPanel.getSelectedJob();
            new Task.Backgroundable(project, "Load builds", false) {

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
    public void update(AnActionEvent event) {
        Job selectedJob = browserPanel.getSelectedJob();
        event.getPresentation().setVisible(selectedJob != null);
    }
}
