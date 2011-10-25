/*
 * Copyright (c) 2011 David Boissier
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
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.JenkinsControlComponent;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

public class OpenPluginSettingsAction extends AnAction {


    public OpenPluginSettingsAction() {
        super("Jenkins Settings", "Edit the Jenkins settings for the current project", GuiUtil.loadIcon("pluginSettings.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        showSettingsFor(getProject(e.getDataContext()));
    }

    private static void showSettingsFor(Project project) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, JenkinsControlComponent.class);
    }

    private static Project getProject(DataContext dataContext) {
        return DataKeys.PROJECT.getData(dataContext);
    }
}
