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

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.JobWithDetails;
import lombok.Value;
import org.codinjutsu.tools.jenkins.exception.NoJobFoundException;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.BuildType;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class ShowLogAction extends AnAction implements DumbAware {

    private static final Icon ICON = AllIcons.Actions.ShowHiddens;//AllIcons.Nodes.Console

    private final BrowserPanel browserPanel;
    private final BuildType buildType;

    public ShowLogAction(BrowserPanel browserPanel, BuildType buildType) {
        super(ICON);
        final ShowLogActionText actionText = getActionText(buildType);
        getTemplatePresentation().setText(actionText.getText());
        getTemplatePresentation().setDescription(actionText.getDescription());
        this.browserPanel = browserPanel;
        this.buildType = buildType;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = ActionUtil.getProject(event);
        final BrowserPanel browserPanelForAction = ActionUtil.getBrowserPanel(event);

        final Job job = browserPanelForAction.getSelectedJob();
        new Task.Backgroundable(project, job.getNameToRenderSingleJob(), false) {

            String consoleContent;

            @Override
            public void onSuccess() {
                if (consoleContent == null) {
                    return;
                }
                GuiUtil.runInSwingThread(() -> {
                    TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(myProject);
                    builder.setViewer(true);
                    ConsoleView consoleView = builder.getConsole();
                    consoleView.print(consoleContent, ConsoleViewContentType.NORMAL_OUTPUT);

                    JPanel panel = new JPanel(new BorderLayout());

                    DefaultActionGroup toolbarActions = new DefaultActionGroup();
                    ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(
                            ActionPlaces.UNKNOWN, toolbarActions, false);
                    panel.add(actionToolbar.getComponent(), BorderLayout.WEST);
                    panel.add(consoleView.getComponent(), BorderLayout.CENTER);
                    actionToolbar.setTargetComponent(panel);

                    toolbarActions.addAll(consoleView.createConsoleActions());
                    toolbarActions.addAction(new ShowJobResultsAsJUnitViewAction(browserPanelForAction));
                    panel.updateUI();

                    final RunContentDescriptor contentDescriptor = new RunContentDescriptor(consoleView, null, panel, myTitle);
                    RunContentManager.getInstance(project).showRunContent(DefaultRunExecutor.getRunExecutorInstance(), contentDescriptor);
                });

            }

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                RequestManager requestManager = browserPanelForAction.getJenkinsManager();
                progressIndicator.setIndeterminate(true);
                try {
                    consoleContent = requestManager.loadConsoleTextFor(job, buildType);
                } catch (NoJobFoundException e) {
                    browserPanelForAction.notifyErrorJenkinsToolWindow(e.getMessage());
                }
            }
        }.queue();

    }

    @Override
    public void update(AnActionEvent event) {
        final Job selectedJob = browserPanel.getSelectedJob();
        final boolean canShowLogForLastBuild = selectedJob != null
                && selectedJob.isBuildable()
                && isLogAvailable(selectedJob)
                && !selectedJob.isInQueue();
        event.getPresentation().setVisible(canShowLogForLastBuild);
    }

    private boolean isLogAvailable(@NotNull Job buildableJob) {
        return buildableJob.getAvailableBuildTypes().contains(buildType);
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

    @Value
    static class ShowLogActionText {

        @Nullable String text;
        @Nullable String description;

    }
}
