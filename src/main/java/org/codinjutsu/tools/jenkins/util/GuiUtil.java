/*
 * Copyright (c) 2012 David Boissier
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
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class GuiUtil {

    private static final String ICON_FOLDER = "/images/";

    private GuiUtil() {
    }


    public static Icon loadIcon(String iconFilename) {
        return IconLoader.findIcon(ICON_FOLDER + iconFilename);
    }


    public static void runInSwingThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
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
}
