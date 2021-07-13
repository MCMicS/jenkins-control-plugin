/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import lombok.Value;
import org.codinjutsu.tools.jenkins.model.BuildType;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ShowLogAction extends AnAction implements DumbAware {

    private static final Icon ICON = AllIcons.Actions.ShowHiddens;//AllIcons.Nodes.Console
    private static final Logger LOG = Logger.getInstance(UploadPatchToJobAction.class.getName());

    private final BuildType buildType;

    public ShowLogAction(BuildType buildType) {
        super(ICON);
        final ShowLogActionText actionText = getActionText(buildType);
        getTemplatePresentation().setText(actionText.getText());
        getTemplatePresentation().setDescription(actionText.getDescription());
        this.buildType = buildType;
    }

    public static boolean isAvailable(@Nullable Job job, @NotNull BuildType buildType) {
        return job != null
                && job.isBuildable()
                && isLogAvailable(job, buildType)
                && !job.isInQueue();
    }

    @NotNull
    static ShowLogActionText getActionText(BuildType buildType) {
        final ShowLogActionText logActionText;
        switch (buildType) {
            case LAST_SUCCESSFUL:
                logActionText = new ShowLogActionText("Show last successful log", "Show last successful build's log");
                break;
            case LAST_FAILED:
                logActionText = new ShowLogActionText("Show last failed log", "Show last failed build's log");
                break;
            case LAST://Fallthrough
            default:
                logActionText = new ShowLogActionText("Show last log", "Show last build's log");
        }
        return logActionText;
    }

    private static boolean isLogAvailable(@NotNull Job buildableJob, @NotNull BuildType buildType) {
        return buildableJob.getAvailableBuildTypes().contains(buildType);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = ActionUtil.getProject(event);
        final BrowserPanel browserPanelForAction = ActionUtil.getBrowserPanel(event);
        final Job job = browserPanelForAction.getSelectedJob();
        final LogToolWindow logToolWindow = new LogToolWindow(project);
        logToolWindow.showLog(buildType, job, browserPanelForAction);
    }

    public boolean isAvailable(@Nullable Job job) {
        return isAvailable(job, buildType);
    }

    @Override
    public void update(AnActionEvent event) {
        final BrowserPanel browserPanel = ActionUtil.getBrowserPanel(event);
        final Job selectedJob = browserPanel.getSelectedJob();
        final boolean canShowLogForLastBuild = isAvailable(selectedJob);
        event.getPresentation().setVisible(canShowLogForLastBuild);
    }

    private boolean isLogAvailable(@NotNull Job buildableJob) {
        return isLogAvailable(buildableJob, buildType);
    }

    @Value
    static class ShowLogActionText {

        @Nullable @NlsActions.ActionText String text;
        @Nullable @NlsActions.ActionDescription String description;

    }
}
