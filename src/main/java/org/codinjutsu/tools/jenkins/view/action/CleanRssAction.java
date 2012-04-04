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
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.jenkins.logic.JenkinsBrowserLogic;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

public class CleanRssAction extends AnAction implements DumbAware {
    private final JenkinsBrowserLogic logic;


    public CleanRssAction(JenkinsBrowserLogic logic) {
        super("Clean last completed builds", "Clean last completed builds", GuiUtil.loadIcon("erase.png"));
        this.logic = logic;
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        logic.cleanRssEntries();
    }
}
