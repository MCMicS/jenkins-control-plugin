package org.codinjutsu.tools.jenkins.view.extension;

import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.view.action.results.BuildTestResultsToolWindow;
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
    public boolean canHandle(@NotNull Build build) {
        return !build.isBuilding();
    }

    @Override
    public void handle(@NotNull Project project, @NotNull Build build) {
        final BuildTestResultsToolWindow jobTestResultsToolWindow = new BuildTestResultsToolWindow(project, build);
        jobTestResultsToolWindow.showInToolWindow();
    }
}
