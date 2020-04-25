package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import org.codinjutsu.tools.jenkins.view.JenkinsWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildStatusSummaryFactory implements StatusBarWidgetProvider {

    public static final String BUILD_STATUS_SUMMARY_ID = "BuildStatusSummary";

    @Nullable
    @Override
    public StatusBarWidget getWidget(@NotNull final Project project) {
        return JenkinsWidget.getInstance(project);
    }
}
