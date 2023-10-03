package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
public final class JenkinsBackgroundTaskFactory {
    @NotNull
    private final Project project;

    JenkinsBackgroundTaskFactory(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    public static JenkinsBackgroundTaskFactory getInstance(@NotNull Project project) {
        final JenkinsBackgroundTaskFactory jenkinsBackgroundTaskFactory = project.getService(
                JenkinsBackgroundTaskFactory.class);
        return jenkinsBackgroundTaskFactory == null ? new JenkinsBackgroundTaskFactory(project) :
                jenkinsBackgroundTaskFactory;
    }

    @NotNull
    public JenkinsBackgroundTask createBackgroundTask(@NlsContexts.ProgressTitle @NotNull String title,
                                                      @NotNull JenkinsBackgroundTask.JenkinsTask jenkinsTask) {
        return createBackgroundTask(title, false, jenkinsTask);
    }

    @NotNull
    public JenkinsBackgroundTask createBackgroundTask(@NlsContexts.ProgressTitle @NotNull String title,
                                                      boolean canBeCancelled,
                                                      @NotNull JenkinsBackgroundTask.JenkinsTask jenkinsTask) {
        return new JenkinsBackgroundTask(project, title, canBeCancelled, jenkinsTask, RequestManager.getInstance(project));
    }
}
