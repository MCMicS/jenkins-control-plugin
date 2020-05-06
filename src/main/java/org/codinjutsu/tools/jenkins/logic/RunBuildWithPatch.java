package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

//@Service
public final class RunBuildWithPatch {

    public static final String PARAMETER_NAME = "patch.diff";
    private static final Logger LOG = Logger.getInstance(RunBuildWithPatch.class.getName());
    private static final String SUFFIX_JOB_NAME_MACROS = "$JobName$";
    private final Project project;

    public RunBuildWithPatch(Project project) {
        this.project = project;
    }

    @NotNull
    public static RunBuildWithPatch getInstance(@NotNull Project project) {
        RunBuildWithPatch service = ServiceManager.getService(project, RunBuildWithPatch.class);
        return service == null ? new RunBuildWithPatch(project) : service;
    }

    public void runBuild(@NotNull Project project, @NotNull Job job, @NotNull VirtualFile patchFile) {
        runBuild(project, job, patchFile, BrowserPanel.getInstance(project)::notifyErrorJenkinsToolWindow);
    }

    public void runBuild(@NotNull Project project, @NotNull Job job, @NotNull VirtualFile patchFile,
                         @NotNull Consumer<String> errorNotifier) {
        if (!job.hasParameter(RunBuildWithPatch.PARAMETER_NAME)) {
            errorNotifier.accept(String.format("Job \"%s\" should has parameter with name \"%s\"",
                    job.getName(), RunBuildWithPatch.PARAMETER_NAME));
        }
        try {
            final JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);
            final VirtualFile preparedFile = prepareFile(BrowserPanel.getInstance(project), patchFile, settings, job);
            if (preparedFile.exists()) {
                new RunPatchFile(project, job, preparedFile).queue();
            } else {
                final String message = String.format("File \"%s\" not exists", preparedFile.getPath());
                LOG.info(message);
                errorNotifier.accept(message);
            }
        } catch (IOException e) {
            final String message = String.format("Build cannot be run: %1$s", e.getMessage());
            LOG.error(message, e);
            errorNotifier.accept(message);
        }
    }

    @NotNull
    VirtualFile prepareFile(BrowserPanel browserPanel, @NotNull VirtualFile file,
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
    String prepareFileContent(@NotNull VirtualFile file, @NotNull String suffix) throws IOException {
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
