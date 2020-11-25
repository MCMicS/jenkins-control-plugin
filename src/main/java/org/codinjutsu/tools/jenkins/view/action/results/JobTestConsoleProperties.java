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

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.config.Storage.PropertiesComponentStorage;
import org.codinjutsu.tools.jenkins.model.Job;
import org.jetbrains.annotations.NotNull;

public class JobTestConsoleProperties extends TestConsoleProperties implements SMCustomMessagesParsing {
    private final RunProfile configuration;
    private final ProcessHandler processHandler;
    private final Job job;

    public JobTestConsoleProperties(Job job, Project project, Executor executor, RunProfile configuration, ProcessHandler processHandler) {
        super(new PropertiesComponentStorage("Jenkins.", PropertiesComponent.getInstance()), project, executor);
        this.job = job;
        this.configuration = configuration;
        this.processHandler = processHandler;
    }

    @Override
    public RunProfile getConfiguration() {
        return configuration;
    }

    @Override
    public OutputToGeneralTestEventsConverter createTestEventsConverter(@NotNull String testFrameworkName,
                                                                        @NotNull TestConsoleProperties consoleProperties) {
        return new OutputToGeneralTestEventsConverter(JobTestResultsToolWindowFactory.TOOL_WINDOW_NAME, this) {
            @Override
            public void onStartTesting() {
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    new JobTestResultsHandler(job, getProject(), getProcessor()).handle();
                    processHandler.detachProcess();
                });
            }
        };
    }
}
