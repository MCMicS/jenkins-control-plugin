package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

@Service
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
    public JenkinsBackgroundTask createBackgroundTask(@Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
                                                      @NotNull JenkinsBackgroundTask.JenkinsTask jenkinsTask) {
        return createBackgroundTask(title, false, jenkinsTask);
    }

    @NotNull
    public JenkinsBackgroundTask createBackgroundTask(@Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
                                                      boolean canBeCancelled,
                                                      @NotNull JenkinsBackgroundTask.JenkinsTask jenkinsTask) {
        return new JenkinsBackgroundTask(project, title, canBeCancelled, jenkinsTask, RequestManager.getInstance(project));
    }
}
