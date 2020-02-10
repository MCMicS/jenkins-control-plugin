package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JenkinsToolWindowFactory implements ToolWindowFactory, DumbAware {

    private static final Icon JENKINS_ICON = GuiUtil.loadIcon("jenkins_logo.png");

    public static final String JENKINS_BROWSER = "Jenkins";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);

        final Content content = ContentFactory.SERVICE.getInstance().createContent(browserPanel, null, false);
        toolWindow.setIcon(JENKINS_ICON);
        toolWindow.setType(ToolWindowType.DOCKED, null);
        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.addContent(content);
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }
}
