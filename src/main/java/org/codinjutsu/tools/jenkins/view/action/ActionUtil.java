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
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.InputEvent;
import java.util.Optional;

public class ActionUtil {

    private ActionUtil() {
    }

    @NotNull
    public static Optional<Project> getProject(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        return Optional.ofNullable(CommonDataKeys.PROJECT.getData(dataContext));
    }

    @NotNull
    public static Optional<BrowserPanel> getBrowserPanel(AnActionEvent event) {
        return getProject(event).map(BrowserPanel::getInstance);
    }

    public static void performAction(@NotNull AnAction action,
                                     @NotNull String place,
                                     @NotNull DataContext dataContext) {
        @Nullable InputEvent event = null;
        final var actionEvent = AnActionEvent.createFromAnAction(action, event, place, dataContext);
        action.actionPerformed(actionEvent);
    }
}
