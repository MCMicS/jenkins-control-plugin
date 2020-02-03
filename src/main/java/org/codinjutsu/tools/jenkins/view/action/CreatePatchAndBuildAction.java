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
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeListManagerEx;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.SelectJobDialog;

import javax.swing.*;
import java.awt.*;

/**
 * CreatePatchAndBuildAction class
 *
 * @author Yuri Novitsky
 */
public class CreatePatchAndBuildAction extends AnAction {

    private Project project;
    private ChangeList[] selectedChangeLists;

    public void actionPerformed(AnActionEvent event) {
        project = ActionUtil.getProject(event);
        DataContext dataContext = event.getDataContext();

        selectedChangeLists = VcsDataKeys.CHANGE_LISTS.getData(dataContext);
        if (selectedChangeLists != null) {
            showDialog();
        }
    }

    private void showDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final BrowserPanel browserPanel = BrowserPanel.getInstance(project);

                SelectJobDialog dialog = new SelectJobDialog(selectedChangeLists, browserPanel.getJobs(), project);
                dialog.setLocationRelativeTo(null);
                dialog.setMaximumSize(new Dimension(300, 200));
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }

    @Override
    public void update(AnActionEvent event) {
        boolean enabled = false;
        project = ActionUtil.getProject(event);
        DataContext dataContext = event.getDataContext();

        selectedChangeLists = VcsDataKeys.CHANGE_LISTS.getData(dataContext);
        if (selectedChangeLists != null && (selectedChangeLists.length > 0)) {
            ChangeListManagerEx changeListManager = (ChangeListManagerEx) ChangeListManager.getInstance(project);
            if (!changeListManager.isInUpdate()) {
                for(ChangeList list: selectedChangeLists) {
                    if (list.getChanges().size() > 0) {
                        enabled = true;
                        break;
                    }
                }
            }
        }
        event.getPresentation().setEnabled(enabled);

    }
}
