package org.codinjutsu.tools.jenkins.view.action;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.settings.ServerConfigurable;
import org.jetbrains.annotations.NotNull;

public class OpenServerSettingsAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        ActionUtil.getProject(event).ifPresent(OpenServerSettingsAction::showSettingsFor);
    }

    private static void showSettingsFor(Project project) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, ServerConfigurable.class);
    }
}
