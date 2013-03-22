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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.JenkinsComponent;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

import javax.swing.*;

public class RefreshNodeAction extends AnAction implements DumbAware {

    private static final Icon REFRESH_ICON = GuiUtil.isUnderDarcula() ? GuiUtil.loadIcon("refresh_dark.png") : GuiUtil.loadIcon("refresh.png");
    private final BrowserPanel logic;


    public RefreshNodeAction(BrowserPanel logic) {
        super("Refresh", "Refresh current node", REFRESH_ICON);
        this.logic = logic;
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = ActionUtil.getProject(event);
        JenkinsComponent jenkinsComponent = project.getComponent(JenkinsComponent.class);

        try {
            logic.loadView(null);//TODO to be refactored
        } catch (Exception ex) {
            jenkinsComponent.notifyErrorJenkinsToolWindow("Unable to refresh: " + ex.getMessage());
        }
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(logic.getJenkins() != null);
    }
}
