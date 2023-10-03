package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.codinjutsu.tools.jenkins.view.JenkinsStatusBarWidget;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class BuildStatusSummaryFactory implements StatusBarWidgetFactory {

    public static final String BUILD_STATUS_SUMMARY_ID = "Jenkins.BuildStatusSummary";

    @NotNull
    @Override
    public String getId() {
        return BUILD_STATUS_SUMMARY_ID;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Jenkins Build Status Summary";
    }

    @NotNull
    @Override
    public StatusBarWidget createWidget(@NotNull Project project) {
        return JenkinsStatusBarWidget.getInstance(project);
    }

}
