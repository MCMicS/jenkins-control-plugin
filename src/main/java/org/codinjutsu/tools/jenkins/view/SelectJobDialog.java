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

package org.codinjutsu.tools.jenkins.view;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.patch.PatchWriter;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBList;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.action.UploadPatchToJob;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SelectJobDialog extends JDialog {

    private static final Logger LOG = Logger.getInstance(UploadPatchToJob.class.getName());
    private static String FILENAME = "jenkins.diff";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox jobsList;
    private JBList changedFilesList;
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
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public static void main(String[] args) {
        SelectJobDialog dialog = new SelectJobDialog(new ChangeList[]{}, null, null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private void fillJobList(List<Job> jobs) {
        if (null != jobs) {
            for (Job job : jobs) {
                if (job.hasParameters() && job.hasParameter(UploadPatchToJob.PARAMETER_NAME)) {
                    listModel.addElement(job.getName());
                }
            }
        }

        jobsList.setModel(listModel);
    }

    private void fillChangedFilesList() {
        final DefaultListModel<String> model = new DefaultListModel<>();

        if (changeLists != null && (changeLists.length > 0)) {
            StringBuilder builder = new StringBuilder();

            int count = 1;
            for (ChangeList changeList : changeLists) {
                builder.append(changeList.getName());
                if (count < changeLists.length) {
                    builder.append(", ");
                }
                if (!changeList.getChanges().isEmpty()) {
                    for (Change change : changeList.getChanges()) {
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

    @NotNull
    private String createPatchFile() throws IOException, VcsException {
        try (FileWriter writer = new FileWriter(FILENAME)) {
            ArrayList<Change> changes = new ArrayList<>();
            if (changeLists.length > 0) {
                for (ChangeList changeList : changeLists) {
                    changes.addAll(changeList.getChanges());
                }
            }
            String base = PatchWriter.calculateBaseForWritingPatch(project, changes).getPath();
            List<FilePatch> patches = IdeaTextPatchBuilder.buildPatch(project, changes, base, false);
            UnifiedDiffWriter.write(project, patches, writer, CodeStyle.getProjectOrDefaultSettings(project).getLineSeparator(), null);
        }
        return FILENAME;
    }

    private void watchJob(BrowserPanel browserPanel, Job job) {
        if (changeLists.length > 0) {
            for (ChangeList list : changeLists) {
                browserPanel.addToWatch(list.getName(), job);
            }
        }
    }

    private void onOK() {
        BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        String patchFileName = null;
        try {
            patchFileName = createPatchFile();
            String selectedJobName = (String) jobsList.getSelectedItem();
            if (selectedJobName != null && !selectedJobName.isEmpty()) {
                final Job selectedJob = browserPanel.getJob(selectedJobName)
                        .filter(Job::hasParameters)
                        .filter(job -> job.hasParameter(UploadPatchToJob.PARAMETER_NAME))
                        .orElse(null);
                if (selectedJob != null) {
                    uploadPatchForJob(selectedJob);
                }
            }
        } catch (Exception e) {
            final String message = String.format("Build cannot be run:  %1$s", e.getMessage());
            LOG.info(message, e);
            browserPanel.notifyErrorJenkinsToolWindow(message);
        }
        Optional.ofNullable(patchFileName).ifPresent(this::deletePatchFile);
        dispose();
    }

    private void uploadPatchForJob(@NotNull Job selectedJob) throws IOException {
        BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        RequestManager requestManager = browserPanel.getJenkinsManager();
        if (selectedJob.hasParameters()) {
            if (selectedJob.hasParameter(UploadPatchToJob.PARAMETER_NAME)) {
                JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);
                Map<String, VirtualFile> files = new HashMap<>();
                VirtualFile virtualFile = UploadPatchToJob.prepareFile(browserPanel, LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(FILENAME)), settings, selectedJob);
                if (virtualFile != null && virtualFile.exists()) {
                    files.put(UploadPatchToJob.PARAMETER_NAME, virtualFile);
                    requestManager.runBuild(selectedJob, settings, files);
                    browserPanel.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                            selectedJob.getName() + " build is on going",
                            selectedJob.getUrl())
                    );

                    watchJob(browserPanel, selectedJob);

                } else {
                    throw new ConfigurationException(String.format("File \"%s\" not found", virtualFile == null ?
                            "null" : virtualFile.getPath()));
                }
            } else {
                throw new ConfigurationException(String.format("Job \"%s\" should has parameter with name \"%s\"", selectedJob.getName(), UploadPatchToJob.PARAMETER_NAME));
            }
        } else {
            throw new ConfigurationException(String.format("Job \"%s\" has no parameters", selectedJob.getName()));
        }
    }

    private boolean deletePatchFile(@NotNull String fileName) {
        File file = new File(fileName);
        return file.delete();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
