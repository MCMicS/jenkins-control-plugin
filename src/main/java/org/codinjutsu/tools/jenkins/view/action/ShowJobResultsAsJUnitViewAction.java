package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.action.results.JobTestResultsToolWindow;

import javax.swing.*;

public class ShowJobResultsAsJUnitViewAction extends AnAction {
    private static final Icon ICON = AllIcons.Actions.GroupByTestProduction;


    public ShowJobResultsAsJUnitViewAction() {
        super("Show test results", "Show test results as JUnit view", ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        Job job = browserPanel.getSelectedJob();

        new JobTestResultsToolWindow(project, job).showMavenToolWindow();
    }

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = BrowserPanel.getInstance(event.getData(PlatformDataKeys.PROJECT)).getSelectedJob();
        event.getPresentation().setVisible(selectedJob != null && selectedJob.isBuildable());
    }
}
