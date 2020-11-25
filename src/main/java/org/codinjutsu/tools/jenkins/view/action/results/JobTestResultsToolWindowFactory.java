package org.codinjutsu.tools.jenkins.view.action.results;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class JobTestResultsToolWindowFactory implements ToolWindowFactory {

    static final String TOOL_WINDOW_ID = "Job test results";
    static final String TOOL_WINDOW_NAME = TOOL_WINDOW_ID;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // filled dynamically
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
