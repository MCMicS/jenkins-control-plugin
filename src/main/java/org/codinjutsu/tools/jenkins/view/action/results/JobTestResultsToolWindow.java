/*
 * Copyright 2014 Dawid Pytel
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

package org.codinjutsu.tools.jenkins.view.action.results;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentContainer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.Optional;

import static org.codinjutsu.tools.jenkins.view.action.results.JobTestResultsToolWindowFactory.TOOL_WINDOW_ID;

public class JobTestResultsToolWindow {

    private final Project project;
    private Job job;


    public JobTestResultsToolWindow(Project project, Job job) {
        this.project = project;
        this.job = job;
    }

    public void showMavenToolWindow() {
        final ConfigurationType configurationType = UnknownConfigurationType.getInstance();
        final ConfigurationFactory configurationFactory = configurationType.getConfigurationFactories()[0];

        final RunConfiguration configuration = new UnknownRunConfiguration(configurationFactory, project);
        final Executor executor = DefaultRunExecutor.getRunExecutorInstance();
        final ProcessHandler processHandler = new MyProcessHandler();
        final TestConsoleProperties consoleProperties = new JobTestConsoleProperties(job, project, executor,
                configuration, processHandler);
        final BaseTestsOutputConsoleView consoleView;
        try {
            consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole(TOOL_WINDOW_ID, processHandler,
                    consoleProperties);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        showInToolWindow(consoleView, job.getNameToRenderSingleJob());
        processHandler.startNotify();
    }

    private void showInToolWindow(ComponentContainer consoleView, String tabName) {
        getToolWindow().ifPresent(toolWindow -> GuiUtil.showInToolWindow(toolWindow, consoleView, tabName));
    }

    @NotNull
    private Optional<ToolWindow> getToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        return Optional.ofNullable(toolWindowManager.getToolWindow(TOOL_WINDOW_ID));
    }

    private static class MyProcessHandler extends ProcessHandler {
        @Override
        protected void destroyProcessImpl() {

        }

        @Override
        protected void detachProcessImpl() {

        }

        @Override
        public boolean detachIsDefault() {
            return true;
        }

        @Nullable
        @Override
        public OutputStream getProcessInput() {
            return null;
        }
    }
}
