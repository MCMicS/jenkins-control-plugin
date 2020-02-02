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
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentContainer;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.OutputStream;

public class JobTestResultsToolWindow {

    private static final String TOOL_WINDOW_ID = "Job test results";
    private static final Icon ICON = AllIcons.Actions.GroupByTestProduction;
    private final Project project;
    private Job job;


    public JobTestResultsToolWindow(Project project, Job job) {
        this.project = project;
        this.job = job;
    }

    public void showMavenToolWindow() {
        ConfigurationType configurationType = UnknownConfigurationType.getInstance();
        final ConfigurationFactory configurationFactory = configurationType.getConfigurationFactories()[0];

        RunConfiguration configuration = new UnknownRunConfiguration(configurationFactory, project);
        Executor executor = new DefaultRunExecutor();
        ProcessHandler processHandler = new MyProcessHandler();
        TestConsoleProperties consoleProperties = new JobTestConsoleProperties(job, project, executor, configuration, processHandler);
        BaseTestsOutputConsoleView consoleView;
        try {
            consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole(TOOL_WINDOW_ID, processHandler, consoleProperties);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        showInToolWindow(consoleView, job.getName());
        processHandler.startNotify();
    }

    private void showInToolWindow(ComponentContainer consoleView, String tabName) {
        ToolWindow toolWindow = getToolWindow();
        toolWindow.activate(null);
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getFactory()
                .createContent(consoleView.getComponent(), tabName, false);
        Disposer.register(content, consoleView);
        contentManager.addContent(content);
        contentManager.setSelectedContent(content);
    }

    private ToolWindow getToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow == null) {
            toolWindow = createToolWindow(toolWindowManager);
        }
        return toolWindow;
    }

    private ToolWindow createToolWindow(ToolWindowManager toolWindowManager) {
        ToolWindow toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, true, ToolWindowAnchor.BOTTOM);
        toolWindow.setIcon(ICON);
        return toolWindow;
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
