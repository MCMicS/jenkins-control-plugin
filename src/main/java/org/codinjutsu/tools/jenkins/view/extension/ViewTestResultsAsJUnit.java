package org.codinjutsu.tools.jenkins.view.extension;

import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.action.results.JobTestResultsToolWindow;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ViewTestResultsAsJUnit implements ViewTestResults {

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Nullable
    @Override
    public String getDescription() {
        return "Show test results as JUnit view";
    }

    @Override
    public boolean canHandle(@NotNull Job job) {
        return job.isBuildable();
    }

    @Override
    public void handle(@NotNull Project project, @NotNull Job job) {
        final JobTestResultsToolWindow jobTestResultsToolWindow = new JobTestResultsToolWindow(project, job);
        jobTestResultsToolWindow.showMavenToolWindow();
    }
}
