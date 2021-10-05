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

package org.codinjutsu.tools.jenkins.view.vcs.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeListManagerEx;
import lombok.Value;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.action.ActionUtil;
import org.codinjutsu.tools.jenkins.view.vcs.SelectJobDialog;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * CreatePatchAndBuildAction class
 *
 * @author Yuri Novitsky
 */
public class CreatePatchAndBuildAction extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        getProjectChangeList(event).ifPresent(this::actionPerformed);
    }

    private void actionPerformed(ProjectChangeList projectChangeList) {
        if (!projectChangeList.getChangeLists().isEmpty()) {
            showDialog(projectChangeList);
        }
    }

    private void showDialog(ProjectChangeList projectChangeList) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final BrowserPanel browserPanel = BrowserPanel.getInstance(projectChangeList.getProject());

                SelectJobDialog dialog = new SelectJobDialog(projectChangeList.getChangeLists(),
                        browserPanel.getAllJobs(), projectChangeList.getProject());
                dialog.setLocationRelativeTo(null);
                dialog.setMaximumSize(new Dimension(300, 200));
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }

    @Override
    public void update(AnActionEvent event) {
        boolean enabled = getProjectChangeList(event).map(this::isEnabled).orElse(Boolean.FALSE);
        event.getPresentation().setEnabled(enabled);
    }

    private boolean isEnabled(ProjectChangeList projectChangeList) {
        boolean enabled = false;

        final Collection<ChangeList> selectedChangeLists = projectChangeList.getChangeLists();
        if (!selectedChangeLists.isEmpty()) {
            ChangeListManagerEx changeListManager = (ChangeListManagerEx) ChangeListManager.getInstance(
                    projectChangeList.getProject());
            if (!changeListManager.isInUpdate()) {
                for (ChangeList list : selectedChangeLists) {
                    if (list.getChanges().size() > 0) {
                        enabled = true;
                        break;
                    }
                }
            }
        }
        return enabled;
    }

    @NotNull
    private Collection<ChangeList> getSelectedChangeLists(DataContext dataContext) {
        final ChangeList[] changeLists = VcsDataKeys.CHANGE_LISTS.getData(dataContext);
        return changeLists == null ? Collections.emptySet() : new ArrayList<>(Arrays.asList(changeLists));
    }

    @NotNull
    private Optional<ProjectChangeList> getProjectChangeList(AnActionEvent event) {
        return ActionUtil.getProject(event)
                .map(project -> new ProjectChangeList(project, getSelectedChangeLists(event.getDataContext())));
    }

    @Value
    private static class ProjectChangeList {
        @NotNull
        private final Project project;
        @NotNull
        private Collection<ChangeList> changeLists;
    }
}
