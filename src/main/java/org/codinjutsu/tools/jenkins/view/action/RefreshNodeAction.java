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
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RefreshNodeAction extends AnAction implements DumbAware {

    private static final Icon REFRESH_ICON = AllIcons.Actions.Refresh;
    private final BrowserPanel browserPanel;


    public RefreshNodeAction(BrowserPanel browserPanel) {
        super("Refresh", "Refresh current node", REFRESH_ICON);
        this.browserPanel = browserPanel;
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        try {
            browserPanel.refreshCurrentView();
        } catch (Exception ex) {
            browserPanel.notifyErrorJenkinsToolWindow("Unable to refresh: " + ex.getMessage());
        }
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(browserPanel.isConfigured());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
