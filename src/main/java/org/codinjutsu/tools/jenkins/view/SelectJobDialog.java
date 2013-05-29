package org.codinjutsu.tools.jenkins.view;

import com.intellij.codeStyle.CodeStyleFacade;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.action.UploadPathToJob;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectJobDialog extends JDialog {

    private static String FILENAME = "jenkins.diff";

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox selectJobList;

    private DefaultComboBoxModel listModel = new DefaultComboBoxModel();

    private Project project;


    public SelectJobDialog(List<Job> jobs, Project project) {

        this.project = project;

        if (null != jobs) {
            if (!jobs.isEmpty()) {
                for(Job job: jobs) {
                    if (job.hasParameters() && job.hasParameter(UploadPathToJob.PARAMETER_NAME)) {
                        listModel.addElement(job.getName());
                    }
                }
            }
        }

        selectJobList.setModel(listModel);

        setContentPane(contentPane);
        setModal(true);

        setTitle("Select jenkins job for build");
        setResizable(false);

        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private boolean createPatch()
    {
        ChangeListManager manager = ChangeListManager.getInstance(project);
        LocalChangeList changeList = manager.getDefaultChangeList();
        try {
            FileWriter writer = new FileWriter(FILENAME);
            List<FilePatch> patches = IdeaTextPatchBuilder.buildPatch(project, changeList.getChanges(), project.getBaseDir().getPresentableUrl(), false);
            UnifiedDiffWriter.write(project, patches, writer, CodeStyleFacade.getInstance(project).getLineSeparator(), null);
            writer.close();
        } catch (VcsException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void onOK() {
        if (createPatch()) {
            BrowserPanel browserPanel = BrowserPanel.getInstance(project);
            RequestManager requestManager = browserPanel.getJenkinsManager();
            String selectedJobName = (String) selectJobList.getSelectedItem();
            if (selectedJobName != null && !selectedJobName.isEmpty()) {
                Job selectedJob = browserPanel.getJob(selectedJobName);
                if (selectedJob != null && selectedJob.hasParameters() && selectedJob.hasParameter(UploadPathToJob.PARAMETER_NAME)) {
                    JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);
                    Map<String, VirtualFile> files = new HashMap<String, VirtualFile>();
                    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(FILENAME));
                    if (virtualFile != null && virtualFile.exists()) {
                        files.put(UploadPathToJob.PARAMETER_NAME, virtualFile);
                        requestManager.runBuild(selectedJob, settings, files);
                        browserPanel.loadSelectedJob();
                    }
                }
            }
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        SelectJobDialog dialog = new SelectJobDialog(null, null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
