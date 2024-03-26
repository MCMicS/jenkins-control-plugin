package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShowBuildLogAction extends AnAction implements DumbAware {

    public ShowBuildLogAction() {
        super("Show Log", "Show current build log", AllIcons.Actions.Show);
    }

    private static boolean isAvailable(@Nullable Build build) {
        return build != null;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        ActionUtil.getProject(event).ifPresent(this::actionPerformed);
    }

    private void actionPerformed(Project project) {
        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        browserPanel.getSelectedBuild().ifPresent(build -> {
            final LogToolWindow logToolWindow = new LogToolWindow(project);
            logToolWindow.showLog(build);
        });
    }

    @Override
    public void update(AnActionEvent event) {
        final boolean canShowLogForBuild = ActionUtil.getBrowserPanel(event).flatMap(BrowserPanel::getSelectedBuild)
                .map(ShowBuildLogAction::isAvailable).orElse(Boolean.FALSE);
        event.getPresentation().setVisible(canShowLogForBuild);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
