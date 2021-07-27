package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.ui.playback.commands.ActionCommand;
import lombok.experimental.UtilityClass;
import org.codinjutsu.tools.jenkins.model.BuildType;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.event.InputEvent;

@UtilityClass
public class JobActions {

    private static void execute(@NonNls @NotNull String actionId,
                                @NotNull Class<? extends AnAction> expectedActionClass) {
        final AnAction action = ActionManager.getInstance().getAction(actionId);
        if (expectedActionClass.isInstance(action)) {
            execute(action, ActionCommand.getInputEvent(actionId));
        }
    }

    private static void execute(@NonNls @NotNull AnAction action, @NotNull InputEvent inputEvent) {
        ActionManager.getInstance().tryToExecute(action, inputEvent, null,
                BrowserPanel.JENKINS_PANEL_PLACE, true);
    }

    @NotNull
    public static JobAction triggerBuild() {
        return new TriggerBuildJob();
    }

    @NotNull
    public static JobAction loadBuilds() {
        return new LoadBuilds();
    }

    @NotNull
    public static JobAction showLastLog() {
        return new ShowLog(BuildType.LAST);
    }

    private static class TriggerBuildJob implements JobAction {

        @Override
        public void execute(@NotNull Job job) {
            if (RunBuildAction.isBuildable(job)) {
                JobActions.execute(RunBuildAction.ACTION_ID, RunBuildAction.class);
            }
        }
    }

    private static class LoadBuilds implements JobAction {

        @Override
        public void execute(@NotNull Job job) {
            if (LoadBuildsAction.isAvailable(job)) {
                JobActions.execute(LoadBuildsAction.ACTION_ID, LoadBuildsAction.class);
            }
        }
    }

    private static class ShowLog implements JobAction {

        private final ShowLogAction action;

        public ShowLog(@NotNull BuildType buildType) {
            this.action = new ShowLogAction(buildType);
        }

        @Override
        public void execute(@NotNull Job job) {
            if (action.isAvailable(job)) {
                JobActions.execute(action, ActionCommand.getInputEvent(null));
            }
        }
    }
}
