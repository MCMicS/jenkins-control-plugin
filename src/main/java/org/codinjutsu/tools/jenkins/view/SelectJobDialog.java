package org.codinjutsu.tools.jenkins.view;

import com.intellij.codeStyle.CodeStyleFacade;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.concurrency.readwrite.WriteActionWorker;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.action.UploadPatchToJob;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SelectJobDialog extends JDialog {

    private static String FILENAME = "jenkins.diff";

    private static final Logger LOG = Logger.getInstance(UploadPatchToJob.class.getName());

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox jobsList;
    private JList changedFilesList;
    private JScrollPane changedFilesPane;

    private DefaultComboBoxModel listModel = new DefaultComboBoxModel();

    private Project project;

    private ChangeList[] changeLists;

    public SelectJobDialog(ChangeList[] changeLists, List<Job> jobs, Project project) {

        this.project = project;

        this.changeLists = changeLists;

        fillJobList(jobs);

        fillChangedFilesList();

        setContentPane(contentPane);
        setModal(true);

        setTitle("Create Patch and build on Jenkins");
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

    private void fillJobList(List<Job> jobs) {
        if (null != jobs) {
            if (!jobs.isEmpty()) {
                for(Job job: jobs) {
                    if (job.hasParameters() && job.hasParameter(UploadPatchToJob.PARAMETER_NAME)) {
                        listModel.addElement(job.getName());
                    }
                }
            }
        }

        jobsList.setModel(listModel);
    }

    private void fillChangedFilesList() {

        DefaultListModel model = new DefaultListModel();

        if (changeLists != null && (changeLists.length > 0)) {
            StringBuilder builder = new StringBuilder();

            int count = 1;
            for(ChangeList changeList: changeLists) {
                builder.append(changeList.getName());
                if (count < changeLists.length) {
                    builder.append(", ");
                }
                if (changeList.getChanges().size() > 0) {
                    for(Change change: changeList.getChanges()) {
                        VirtualFile virtualFile = change.getVirtualFile();
                        if (null != virtualFile) {
                            model.addElement(virtualFile.getPath());
                        }
                    }
                }
                count++;
            }

            changedFilesPane.setBorder(IdeBorderFactory.createTitledBorder(String.format("Changelists: %s", builder.toString()), true));

        }

        changedFilesList.setModel(model);
    }

    private boolean createPatch() throws IOException, VcsException {
        FileWriter writer = new FileWriter(FILENAME);
        ArrayList<Change> changes = new ArrayList<Change>();
        if (changeLists.length > 0) {
            for(ChangeList changeList: changeLists) {
                changes.addAll(changeList.getChanges());
            }
        }
        List<FilePatch> patches = IdeaTextPatchBuilder.buildPatch(project, changes, project.getBaseDir().getPresentableUrl(), false);
        UnifiedDiffWriter.write(project, patches, writer, CodeStyleFacade.getInstance(project).getLineSeparator(), null);
        writer.close();

        return true;
    }

    private void onOK() {
        BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        try {
            if (createPatch()) {
                RequestManager requestManager = browserPanel.getJenkinsManager();
                String selectedJobName = (String) jobsList.getSelectedItem();
                if (selectedJobName != null && !selectedJobName.isEmpty()) {
                    Job selectedJob = browserPanel.getJob(selectedJobName);
                    if (selectedJob != null) {
                        if (selectedJob.hasParameters()) {
                            if (selectedJob.hasParameter(UploadPatchToJob.PARAMETER_NAME)) {
                                JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);
                                Map<String, VirtualFile> files = new HashMap<String, VirtualFile>();
                                VirtualFile virtualFile = UploadPatchToJob.prepareFile(browserPanel, LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(FILENAME)), settings, selectedJob);
                                if (virtualFile != null && virtualFile.exists()) {
                                    files.put(UploadPatchToJob.PARAMETER_NAME, virtualFile);
                                    requestManager.runBuild(selectedJob, settings, files);
                                    //browserPanel.loadSelectedJob();
                                    browserPanel.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                                        selectedJob.getName() + " build is on going",
                                        selectedJob.getUrl())
                                    );
                                } else {
                                    throw new ConfigurationException(String.format("File \"%s\" not found", virtualFile.getPath()));
                                }
                            } else {
                                throw new ConfigurationException(String.format("Job \"%s\" should has parameter with name \"%s\"", selectedJob.getName(), UploadPatchToJob.PARAMETER_NAME));
                            }
                        } else {
                            throw new ConfigurationException(String.format("Job \"%s\" has no parameters", selectedJob.getName()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = String.format("Build cannot be run: " + e.getMessage());
            LOG.info(message);
            browserPanel.notifyErrorJenkinsToolWindow(message);
        }

        deletePatchFile();

        dispose();

    }

    private void deletePatchFile() {
        File file = new File(FILENAME);
        file.delete();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        SelectJobDialog dialog = new SelectJobDialog(new ChangeList[]{}, null, null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
