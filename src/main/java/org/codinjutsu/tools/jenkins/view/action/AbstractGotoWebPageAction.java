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

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserPanel;

abstract class AbstractGotoWebPageAction extends AnAction implements DumbAware {
    final JenkinsBrowserPanel jenkinsBrowserPanel;


    AbstractGotoWebPageAction(String label,
                              String description,
                              String iconFilename,
                              JenkinsBrowserPanel jenkinsBrowserPanel) {
        super(label, description, GuiUtil.loadIcon(iconFilename));
        this.jenkinsBrowserPanel = jenkinsBrowserPanel;
    }


    protected abstract String getUrl();


    @Override
    public void actionPerformed(AnActionEvent event) {
        BrowserUtil.launchBrowser(getUrl());
    }
}
