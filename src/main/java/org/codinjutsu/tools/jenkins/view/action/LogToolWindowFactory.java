package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class LogToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return false;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // filled by Action
    }

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        final String title = "Jenkins Logs";
        toolWindow.setStripeTitle(title);
    }
}
