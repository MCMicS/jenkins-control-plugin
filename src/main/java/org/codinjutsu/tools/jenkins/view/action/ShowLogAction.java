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
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.JenkinsToolWindowFactory;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ShowLogAction extends AnAction implements DumbAware {

    private static final Icon ICON = GuiUtil.loadIcon("show_log.png");

    private final BrowserPanel browserPanel;


    public ShowLogAction(BrowserPanel browserPanel) {
        super("Show log", "Show last build's log", ICON);
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = ActionUtil.getProject(event);

        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        final Job job = browserPanel.getSelectedJob();
        new Task.Backgroundable(project, job.getName(), false) {

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
                    toolbarActions.addAction(new ShowJobResultsAsJUnitViewAction(browserPanel));
                    panel.updateUI();

                    final RunContentDescriptor contentDescriptor = new RunContentDescriptor(consoleView, null, panel, myTitle);

                    RunContentManager.getInstance(project).showRunContent(DefaultRunExecutor.getRunExecutorInstance(), contentDescriptor);
                });

            }

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                RequestManager requestManager = browserPanel.getJenkinsManager();
                progressIndicator.setIndeterminate(true);
                consoleContent = requestManager.loadConsoleTextFor(job);
            }
        }.queue();

    }

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = browserPanel.getSelectedJob();
        event.getPresentation().setVisible(selectedJob != null && selectedJob.isBuildable());
    }
}
