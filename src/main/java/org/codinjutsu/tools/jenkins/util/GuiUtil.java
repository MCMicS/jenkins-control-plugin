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

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.net.URL;

public class GuiUtil {

    private static final String ICON_FOLDER = "/images/";

    private GuiUtil() {
    }


    public static Icon loadIcon(String iconFilename) {
        return IconLoader.findIcon(ICON_FOLDER + iconFilename);
    }


    public static Icon loadIcon(String parentPath, String iconFilename) {
        return IconLoader.findIcon(parentPath + iconFilename);
    }

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

        JComponent actionToolbar = ActionManager.getInstance()
                .createActionToolbar(toolBarName, actionGroup, true).getComponent();
        toolWindowPanel.setToolbar(actionToolbar);
    }

    public static boolean isUnderDarcula() {//keep it for backward compatibility
        return UIManager.getLookAndFeel().getName().contains("Darcula");
    }

    public static URL getIconResource(String iconFilename) {
        return GuiUtil.class.getResource(ICON_FOLDER + iconFilename);
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
