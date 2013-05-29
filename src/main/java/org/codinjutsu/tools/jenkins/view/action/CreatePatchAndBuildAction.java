package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ui.ChangesListView;
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
    ChangeList[] selectedChangeLists;

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

}
