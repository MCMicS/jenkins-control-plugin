package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.JenkinsWindowManager;
import org.jetbrains.annotations.NotNull;

public class ReloadConfigurationAction extends DumbAwareAction {
    public static final String ACTION_ID = "Jenkins.ReloadConfiguration";
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ActionUtil.getProject(event).ifPresent(ReloadConfigurationAction::reloadConfiguration);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    public static void reloadConfiguration(@NotNull Project project) {
        JenkinsWindowManager.getInstance(project).ifPresent(JenkinsWindowManager::reloadConfiguration);
    }
}
