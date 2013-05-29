package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.codeStyle.CodeStyleFacade;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.SelectJobDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * CreatePatchAndBuildAction class
 *
 * @author Yuri Novitsky
 */
public class CreatePatchAndBuildAction extends AnAction {

    private Project project;

    public void actionPerformed(AnActionEvent event) {

        project = ActionUtil.getProject(event);

        showDialog();

    }

    private void showDialog() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                final BrowserPanel browserPanel = BrowserPanel.getInstance(project);

                SelectJobDialog dialog = new SelectJobDialog(browserPanel.getJobs(), project);
                dialog.setLocationRelativeTo(null);
                dialog.setMaximumSize(new Dimension(300, 200));
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }

}
