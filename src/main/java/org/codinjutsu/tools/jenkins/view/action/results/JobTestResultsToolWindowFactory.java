package org.codinjutsu.tools.jenkins.view.action.results;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JobTestResultsToolWindowFactory implements ToolWindowFactory {

    static final String TOOL_WINDOW_ID = "Job test results";

    private static final Icon ICON = AllIcons.Actions.GroupByTestProduction;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(ICON);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return false;
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return true;
    }
}
