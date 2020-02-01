package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.action.results.JobTestResultsToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ShowJobResultsAsJUnitViewAction extends AnAction {
    private static final Icon ICON = AllIcons.Actions.GroupByTestProduction;
    @NotNull
    private final BrowserPanel browserPanel;

    public ShowJobResultsAsJUnitViewAction(@NotNull BrowserPanel browserPanel) {
        super("Show test results", "Show test results as JUnit view", ICON);
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Job job = browserPanel.getSelectedJob();
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        new JobTestResultsToolWindow(project, job).showMavenToolWindow();
    }

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = browserPanel.getSelectedJob();
        event.getPresentation().setVisible(selectedJob != null && selectedJob.isBuildable());
    }
}
