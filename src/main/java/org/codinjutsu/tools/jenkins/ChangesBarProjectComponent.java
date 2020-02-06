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

package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListDecorator;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * org.codinjutsu.tools.jenkins.ChangesBarProjectComponent
 *
 * @author Yuri Novitsky
 */
public class ChangesBarProjectComponent implements ChangeListDecorator {

    private Project project;

    public ChangesBarProjectComponent(Project project) {
        this.project = project;
    }

    @NotNull
    public String getComponentName() {
        return "org.codinjutsu.tools.jenkins.ChangesBarProjectComponent";
    }


    @Override
    public void decorateChangeList(LocalChangeList localChangeList, ColoredTreeCellRenderer coloredTreeCellRenderer, boolean b, boolean b2, boolean b3) {
        BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        //browserPanel.watch();
        Map<String, Job> jobs = browserPanel.getWatched();
        if (jobs.containsKey(localChangeList.getName())) {
            Build build = jobs.get(localChangeList.getName()).getLastBuild();
            String status = build.getStatus().getStatus();
            if (build.isBuilding()) {
                status = "Running";
            }
            coloredTreeCellRenderer.append(String.format(" - last build #%d: %s", build.getNumber(), status), SimpleTextAttributes.GRAYED_ATTRIBUTES);
            coloredTreeCellRenderer.repaint();
        }
    }
}
