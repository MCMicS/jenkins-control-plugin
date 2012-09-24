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

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.logic.JenkinsBrowserLogic;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

public class UnsetJobAsFavoriteAction extends AnAction implements DumbAware {

    private JenkinsBrowserLogic jenkinsBrowserLogic;

    public UnsetJobAsFavoriteAction(JenkinsBrowserLogic jenkinsBrowserLogic) {
        super("Unset as Favorite", "Unset the selected job as favorite", GuiUtil.loadIcon("star_delete.png"));
        this.jenkinsBrowserLogic = jenkinsBrowserLogic;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Job selectedJob = jenkinsBrowserLogic.getSelectedJob();
        jenkinsBrowserLogic.removeFavorite(selectedJob);

        Project project = ActionUtil.getProject(event);
        PropertiesComponent projectProperties = PropertiesComponent.getInstance(project);

        String favoriteViewPropertyValue = projectProperties.getValue("favorite_view");

        projectProperties.setValue("favorite_view", StringUtils.remove(favoriteViewPropertyValue, selectedJob.getName() + ";"));
    }

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = jenkinsBrowserLogic.getSelectedJob();
        event.getPresentation().setVisible(selectedJob != null && jenkinsBrowserLogic.getBrowserPreferences().isAFavoriteJob(selectedJob.getName()));
    }
}
