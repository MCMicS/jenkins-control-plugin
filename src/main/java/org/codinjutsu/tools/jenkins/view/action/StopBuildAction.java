package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.logic.RefreshBuilds;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

import javax.swing.*;

/**
 * Created by marcin on 06.10.15.
 */

public class StopBuildAction extends AnAction implements DumbAware {

    private static final Icon STOP_ICON = AllIcons.Actions.Suspend;
    private static final Logger LOG = Logger.getLogger(RunBuildAction.class.getName());

    private final BrowserPanel browserPanel;


    public StopBuildAction(BrowserPanel browserPanel) {
        super("Stop on Jenkins", "Stop a build on Jenkins Server", STOP_ICON);
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = ActionUtil.getProject(event);

        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        try {
            final Job job = browserPanel.getSelectedJob();
            new Task.Backgroundable(project, "Stopping build", false){

                @Override
                public void onSuccess() {
                    browserPanel.loadJob(job);
                    new RefreshBuilds(project);
                }

                @Override
                public void run(ProgressIndicator progressIndicator) {
                    RequestManager requestManager = browserPanel.getJenkinsManager();
                    requestManager.stopBuild(job.getLastBuild());
                }
            }.queue();

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            browserPanel.notifyErrorJenkinsToolWindow("Build cannot be run: " + ex.getMessage());
        }
    }

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = browserPanel.getSelectedJob();
        event.getPresentation().setVisible(selectedJob != null && selectedJob.isBuildable());
    }
}
