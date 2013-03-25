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
import org.codinjutsu.tools.jenkins.logic.RssLogic;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

public class RefreshRssAction extends AnAction implements DumbAware {


    public RefreshRssAction() {
        super("Refresh last completed builds", "Refresh last completed builds from Jenkins server", GuiUtil.loadIcon("rss.png"));
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = ActionUtil.getProject(event);
        RssLogic.getInstance(project).loadLatestBuilds(true);
    }
}
