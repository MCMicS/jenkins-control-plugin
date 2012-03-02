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

package org.codinjutsu.tools.jenkins.view.action.search;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.SystemInfo;
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserPanel;
import org.codinjutsu.tools.jenkins.view.JobSearchComponent;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class OpenJobSearchPanelAction extends AnAction {//TODO need to move search component into the JenkinsBrowserPanel

    private JobSearchComponent searchComponent;

    public OpenJobSearchPanelAction(JenkinsBrowserPanel jenkinsBrowserPanel) {
        this(jenkinsBrowserPanel, true);
    }

    public OpenJobSearchPanelAction(JenkinsBrowserPanel jenkinsBrowserPanel, boolean installSearchToolbar) {
        searchComponent = new JobSearchComponent(jenkinsBrowserPanel, installSearchToolbar);
        searchComponent.setVisible(false);
        jenkinsBrowserPanel.addComponentToUtilityPanel(searchComponent);

        registerCustomShortcutSet(KeyEvent.VK_F, SystemInfo.isMac ? InputEvent.META_MASK : InputEvent.CTRL_MASK, jenkinsBrowserPanel);

    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        searchComponent.setVisible(true);
        searchComponent.forceFocusForSearchTextField();
    }

}
