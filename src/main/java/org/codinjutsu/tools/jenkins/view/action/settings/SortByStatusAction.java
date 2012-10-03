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

package org.codinjutsu.tools.jenkins.view.action.settings;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.jenkins.logic.BrowserLogic;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

public class SortByStatusAction extends ToggleAction implements DumbAware {

    private boolean sortedByStatus = false;
    private final BrowserLogic browserLogic;

    public SortByStatusAction(BrowserLogic browserLogic) {
        super("Sort by Build Status", "Fail, Unstable, Success, ...", GuiUtil.loadIcon("arrow_up.png"));
        this.browserLogic = browserLogic;
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return sortedByStatus;
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean sorted) {
        sortedByStatus = sorted;
        browserLogic.getJenkinsBrowserPanel().setSortedByStatus(sorted);
    }

}
