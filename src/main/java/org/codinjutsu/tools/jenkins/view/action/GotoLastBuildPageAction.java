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

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

public class GotoLastBuildPageAction extends AbstractGotoWebPageAction {

    public GotoLastBuildPageAction(BrowserPanel browserPanel) {
        super("Go to the latest build page",
                "Open the latest build page in a web browser",
                browserPanel);
    }


    @Override
    public String getUrl() {
        Job job = browserPanel.getSelectedJob();
        return job.getLastBuild().getUrl();
    }


    @Override
    public void update(AnActionEvent event) {
        Job job = browserPanel.getSelectedJob();
        event.getPresentation().setVisible(job != null
                && job.getLastBuild() != null
                && !job.isInQueue());
    }
}