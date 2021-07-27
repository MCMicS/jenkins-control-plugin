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

package org.codinjutsu.tools.jenkins.util;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.ComponentContainer;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GuiUtil {

    public static void runInSwingThread(Runnable runnable) {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            runnable.run();
        } else {
            application.invokeLater(runnable);
        }
    }

    public static void runInSwingThread(final Task.Backgroundable task){
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            task.queue();
        } else {
            application.invokeLater(new TaskRunner(task));
        }
    }

    public static void installActionGroupInToolBar(ActionGroup actionGroup,
                                                   SimpleToolWindowPanel toolWindowPanel,
                                                   ActionManager actionManager, String toolBarName) {
        if (actionManager == null) {
            return;
        }

        final ActionToolbar actionToolbar = ActionManager.getInstance() .createActionToolbar(toolBarName, actionGroup, true);
        actionToolbar.setTargetComponent(toolWindowPanel.getComponent());
        toolWindowPanel.setToolbar(actionToolbar.getComponent());
    }

    public static void showInToolWindow(ToolWindow toolWindow, ComponentContainer consoleView, String tabName) {
        showInToolWindow(toolWindow, consoleView.getComponent(), consoleView, tabName);
    }

    public static void showInToolWindow(ToolWindow toolWindow, JComponent toolWindowComponent,
                                        @NotNull Disposable toolWindowDisposable, String tabName) {
        runInSwingThread(() -> {
            toolWindow.setAvailable(true, null);
            toolWindow.activate(null);
            toolWindow.show(null);
            ContentManager contentManager = toolWindow.getContentManager();
            Content content = contentManager.getFactory().createContent(toolWindowComponent, tabName, false);
            Disposer.register(content, toolWindowDisposable);
            contentManager.addContent(content);
            contentManager.setSelectedContent(content);
        });
    }

    private static class TaskRunner implements Runnable{
        private final Task.Backgroundable task;

        public TaskRunner(Task.Backgroundable task) {
            this.task = task;
        }

        @Override
        public void run() {
            task.queue();
        }
    }
}
