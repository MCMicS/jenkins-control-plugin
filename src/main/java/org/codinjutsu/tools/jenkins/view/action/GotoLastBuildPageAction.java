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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserPanel;

public class GotoLastBuildPageAction extends AbstractGotoWebPageAction {

    public GotoLastBuildPageAction(JenkinsBrowserPanel jenkinsBrowserPanel) {
        super("Go to the latest build page",
                "Open the latest build page in a web browser",
                "page_gear.png", jenkinsBrowserPanel);
    }


    @Override
    public String getUrl() {
        Job job = jenkinsBrowserPanel.getSelectedJob();
        return job.getLastBuild().getUrl();
    }


    @Override
    public void update(AnActionEvent event) {
        Job job = jenkinsBrowserPanel.getSelectedJob();
        event.getPresentation().setEnabled(job != null
                && job.getLastBuild() != null
                && !job.isInQueue());
    }
}