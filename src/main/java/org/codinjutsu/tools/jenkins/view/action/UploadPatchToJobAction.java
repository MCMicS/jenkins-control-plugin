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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.codinjutsu.tools.jenkins.logic.RunBuildWithPatch;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static org.codinjutsu.tools.jenkins.view.BrowserPanel.POPUP_PLACE;

/**
 * Description
 *
 * @author Yuri Novitsky
 */
public class UploadPatchToJobAction extends AnAction implements DumbAware {

    private static final Logger LOG = Logger.getInstance(UploadPatchToJobAction.class.getName());
    private static final Icon EXECUTE_ICON = AllIcons.Actions.Execute;
    private BrowserPanel browserPanel;


    public UploadPatchToJobAction(BrowserPanel browserPanel) {
        super("Upload Patch and build", "Upload Patch to the job and build", EXECUTE_ICON);
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        ActionUtil.getProject(event).ifPresent(project -> actionPerformed(project, BrowserPanel.getInstance(project)));
    }

    private void actionPerformed(Project project, BrowserPanel browserPanel) {
        String message = "";
        try {
            Job job = browserPanel.getSelectedJob();
            if (job.hasParameter(RunBuildWithPatch.PARAMETER_NAME)) {
                final VirtualFile selectedFile = FileChooser.chooseFile(new FileChooserDescriptor(true,
                        false, false, false, false,
                        false), browserPanel, project, null);
                if (selectedFile != null) {
                    ApplicationManager.getApplication().invokeLater(
                            () -> RunBuildWithPatch.getInstance(project).runBuild(project, job, selectedFile,
                                    this::logError),
                            ModalityState.nonModal());
                }
            } else {
                message = String.format("Job \"%s\" should has parameter with name \"%s\"", job.getNameToRenderSingleJob(),
                        RunBuildWithPatch.PARAMETER_NAME);
            }

        } catch (Exception e) {
            message = String.format("Build cannot be run: %1$s", e.getMessage());
            LOG.error(message, e);
        }

        if (!message.isEmpty()) {
            logError(message);
        }
    }

    private void logError(String message) {
        LOG.info(message);
        browserPanel.notifyErrorJenkinsToolWindow(message);
    }

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = browserPanel.getSelectedJob();
        final boolean isPatchUploadable = selectedJob != null && selectedJob.hasParameters() && selectedJob.hasParameter(RunBuildWithPatch.PARAMETER_NAME);
        if (event.getPlace().equals(POPUP_PLACE)) {
            event.getPresentation().setVisible(isPatchUploadable);
        } else {
            event.getPresentation().setEnabled(isPatchUploadable);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
