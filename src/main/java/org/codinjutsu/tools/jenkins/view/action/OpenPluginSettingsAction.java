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

package org.codinjutsu.tools.jenkins.view.action;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.JenkinsComponent;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;

public class OpenPluginSettingsAction extends AnAction implements DumbAware {


    private static final Icon SETTINGS_ICON = GuiUtil.isUnderDarcula() ? GuiUtil.loadIcon("settings_dark.png") : GuiUtil.loadIcon("settings.png");

    public OpenPluginSettingsAction() {
        super("Jenkins Settings", "Edit the Jenkins settings for the current project", SETTINGS_ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        showSettingsFor(ActionUtil.getProject(event));
    }

    private static void showSettingsFor(Project project) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, JenkinsComponent.class);
    }
}
