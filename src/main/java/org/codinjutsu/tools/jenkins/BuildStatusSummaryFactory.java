package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.codinjutsu.tools.jenkins.view.JenkinsWidget;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class BuildStatusSummaryFactory implements StatusBarWidgetFactory {

    public static final String BUILD_STATUS_SUMMARY_ID = "BuildStatusSummary";

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

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @NotNull
    @Override
    public StatusBarWidget createWidget(@NotNull Project project) {
        return JenkinsWidget.getInstance(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
