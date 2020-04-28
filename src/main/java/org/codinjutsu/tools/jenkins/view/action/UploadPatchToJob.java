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
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.ExecutorService;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.codinjutsu.tools.jenkins.view.BrowserPanel.POPUP_PLACE;
import static org.codinjutsu.tools.jenkins.view.action.RunBuildAction.BUILD_STATUS_UPDATE_DELAY;

/**
 * Description
 *
 * @author Yuri Novitsky
 */
public class UploadPatchToJob extends AnAction implements DumbAware {

    public static final String PARAMETER_NAME = "patch.diff";
    public static final String SUFFIX_JOB_NAME_MACROS = "$JobName$";

    private static final Logger LOG = Logger.getInstance(UploadPatchToJob.class.getName());
    private BrowserPanel browserPanel;

    private static final Icon EXECUTE_ICON = AllIcons.Actions.Execute;


    public UploadPatchToJob(BrowserPanel browserPanel) {
        super("Upload Patch and build", "Upload Patch to the job and build", EXECUTE_ICON);
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = ActionUtil.getProject(event);
        final BrowserPanel browserPanel = ActionUtil.getBrowserPanel(event);
        String message = "";

        try {
            Job job = browserPanel.getSelectedJob();
            if (job.hasParameters()) {
                if (job.hasParameter(PARAMETER_NAME)) {
                    final VirtualFile selectedFile = FileChooser.chooseFile(new FileChooserDescriptor(true,
                            false, false, false, false,
                            false), browserPanel, project, null);
                    if (selectedFile != null) {
                        ApplicationManager.getApplication().invokeLater(() -> runBuild(selectedFile, project, job),
                                ModalityState.NON_MODAL);
                    }
                } else {
                    message = String.format("Job \"%s\" should has parameter with name \"%s\"", job.getName(), PARAMETER_NAME);
                }
            } else {
                message = String.format("Job \"%s\" has no parameters", job.getName());
            }

        } catch (Exception e) {
            message = String.format("Build cannot be run: %1$s", e.getMessage());
            LOG.error(message, e);
        }

        if (!message.isEmpty()) {
            LOG.info(message);
            browserPanel.notifyErrorJenkinsToolWindow(message);
        }
    }

    private void runBuild(@NotNull VirtualFile patchFile, @NotNull Project project, @NotNull Job job) {
        try {
            final JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);
            final VirtualFile preparedFile = prepareFile(browserPanel, patchFile, settings, job);
            if (preparedFile.exists()) {
                runPatchFile(project, job, preparedFile);
            } else {
                final String message = String.format("File \"%s\" not exists", preparedFile.getPath());
                LOG.info(message);
                browserPanel.notifyErrorJenkinsToolWindow(message);
            }
        } catch (IOException e) {
            final String message = String.format("Build cannot be run: %1$s", e.getMessage());
            LOG.error(message, e);
            browserPanel.notifyErrorJenkinsToolWindow(message);
        }
    }

    public static void runPatchFile(@NotNull Project project, @NotNull Job job,
                                    @NotNull VirtualFile preparedFilePatchFile) {
        new RunPatchFile(project, job, preparedFilePatchFile).queue();
    }

    @NotNull
    public static VirtualFile prepareFile(BrowserPanel browserPanel, @NotNull VirtualFile file,
                                          JenkinsAppSettings settings, Job job) throws IOException {
        if (file.exists()) {
            final String suffix = settings.getSuffix().replace(SUFFIX_JOB_NAME_MACROS, job.getName());
            final String preparedContent = prepareFileContent(file, suffix);

            WriteAction.run(() -> {
                OutputStream outputStream = file.getOutputStream(browserPanel);
                outputStream.write(preparedContent.getBytes(StandardCharsets.UTF_8));
                outputStream.close();
            });
            return file;
        }
        return file;
    }

    @NotNull
    static String prepareFileContent(@NotNull VirtualFile file, @NotNull String suffix) throws IOException {
        final StringBuilder builder = new StringBuilder();
        if (file.exists()) {
            String line;
            try (InputStream stream = file.getInputStream();
                 InputStreamReader streamReader = new InputStreamReader(stream);
                 BufferedReader bufferReader = new BufferedReader(streamReader)) {
                while ((line = bufferReader.readLine()) != null) {
                    if (line.startsWith("Index: ") && !line.startsWith("Index: " + suffix)) {
                        line = line.replaceFirst("^(Index: )(.+)", "$1" + suffix + "$2");
                    }
                    if (line.startsWith("--- ") && !line.startsWith("--- " + suffix)) {
                        line = line.replaceFirst("^(--- )(.+)", "$1" + suffix + "$2");
                    }
                    if (line.startsWith("+++ ") && !line.startsWith("+++ " + suffix)) {
                        line = line.replaceFirst("^(\\+\\+\\+ )(.+)", "$1" + suffix + "$2");
                    }
                    builder.append(line);
                    builder.append("\r\n");
                }
            }
        }

        return builder.toString();
    }

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = browserPanel.getSelectedJob();
        final boolean isPatchUploadable = selectedJob != null && selectedJob.hasParameters() && selectedJob.hasParameter(PARAMETER_NAME);
        if (event.getPlace().equals(POPUP_PLACE)) {
            event.getPresentation().setVisible(isPatchUploadable);
        } else {
            event.getPresentation().setEnabled(isPatchUploadable);
        }
    }

    private static class RunPatchFile extends Task.Backgroundable {

        @NotNull
        private final VirtualFile patchFile;
        @NotNull
        private final BrowserPanel browserPanel;
        @NotNull
        private final Project project;
        @NotNull
        private final Job job;

        public RunPatchFile(@NotNull Project project, @NotNull Job job, @NotNull VirtualFile patchFile) {
            super(project, "Running build with Patch file", false);
            this.patchFile = patchFile;
            this.browserPanel = BrowserPanel.getInstance(project);
            this.project = project;
            this.job = job;
        }

        @Override
        public void onSuccess() {
            notifyOnGoingMessage(job);
        }

        @Override
        public void onThrowable(@NotNull Throwable error) {
            super.onThrowable(error);
            final String message = String.format("Build cannot be run: %1$s", error.getMessage());
            LOG.error(message, error);
            browserPanel.notifyErrorJenkinsToolWindow(message);
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            progressIndicator.setIndeterminate(true);
            RequestManager requestManager = browserPanel.getJenkinsManager();

            final JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);
            final Map<String, VirtualFile> files = new HashMap<>(Collections.singletonMap(PARAMETER_NAME, patchFile));
            requestManager.runBuild(job, settings, files);
            //browserPanel.loadJob(job);
            browserPanel.refreshCurrentView();
        }

        private void notifyOnGoingMessage(Job job) {
            browserPanel.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                    job.getName() + " build is on going", job.getUrl()));
        }
    }
}
