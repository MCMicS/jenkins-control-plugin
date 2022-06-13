package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

public class JenkinsToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final String JENKINS_BROWSER = "Jenkins";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        browserPanel.initGui();

        final Content content = ContentFactory.getInstance().createContent(browserPanel, null, false);
        toolWindow.setType(ToolWindowType.DOCKED, null);
        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.addContent(content);
    }
}
