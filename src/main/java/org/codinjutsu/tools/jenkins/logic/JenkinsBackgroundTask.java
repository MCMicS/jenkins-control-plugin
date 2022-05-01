package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.exception.JenkinsPluginRuntimeException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public final class JenkinsBackgroundTask extends Task.Backgroundable {

    private static final Logger LOG = Logger.getInstance(JenkinsBackgroundTask.class.getName());

    private final Project project;
    private final JenkinsTask jenkinsTask;
    private final RequestManagerInterface requestManager;

    public JenkinsBackgroundTask(@NotNull Project project,
                                 @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
                                 @NotNull JenkinsBackgroundTask.JenkinsTask jenkinsTask,
                                 @NotNull RequestManagerInterface requestManager) {
        this(project, title, false, jenkinsTask, requestManager);
    }

    public JenkinsBackgroundTask(@NotNull Project project,
                                 @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String title,
                                 boolean canBeCancelled,
                                 @NotNull JenkinsBackgroundTask.JenkinsTask jenkinsTask,
                                 @NotNull RequestManagerInterface requestManager) {
        super(project, title, canBeCancelled, JenkinsBackgroundLoadingOption.INSTANCE);
        this.project = project;
        this.requestManager = requestManager;
        this.jenkinsTask = jenkinsTask;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        progressIndicator.setIndeterminate(true);
        try {
            jenkinsTask.run(requestManager);
        } catch (JenkinsPluginRuntimeException jenkinsPluginRuntimeException) {
            handleJenkinsPluginException(jenkinsPluginRuntimeException);
            throw jenkinsPluginRuntimeException;
        }
    }

    private void handleJenkinsPluginException(JenkinsPluginRuntimeException jenkinsPluginRuntimeException) {
        LOG.warn(jenkinsPluginRuntimeException);
        JenkinsNotifier.getInstance(project).error(jenkinsPluginRuntimeException.getMessage());
    }

    @Override
    public void onCancel() {
        try {
            super.onCancel();
            jenkinsTask.onCancel();
        } catch (JenkinsPluginRuntimeException jenkinsPluginRuntimeException) {
            handleJenkinsPluginException(jenkinsPluginRuntimeException);
        }
    }

    @Override
    public void onSuccess() {
        try {
            super.onSuccess();
            jenkinsTask.onSuccess();
        } catch (JenkinsPluginRuntimeException jenkinsPluginRuntimeException) {
            handleJenkinsPluginException(jenkinsPluginRuntimeException);
        }
    }

    @Override
    public void onThrowable(@NotNull Throwable error) {
        try {
            if (!(error instanceof JenkinsPluginRuntimeException)) {
                super.onThrowable(error);
            }
            jenkinsTask.onThrowable(error);
        } catch (JenkinsPluginRuntimeException jenkinsPluginRuntimeException) {
            handleJenkinsPluginException(jenkinsPluginRuntimeException);
        }
    }

    @Override
    public void onFinished() {
        try {
            super.onFinished();
            jenkinsTask.onFinished();
        } catch (JenkinsPluginRuntimeException jenkinsPluginRuntimeException) {
            handleJenkinsPluginException(jenkinsPluginRuntimeException);
        }
    }

    public interface JenkinsTask {

        void run(@NotNull RequestManagerInterface requestManager);

        /**
         * @see com.intellij.openapi.progress.Task#onCancel()
         */
        default void onCancel() {
        }

        /**
         * @see com.intellij.openapi.progress.Task#onSuccess()
         */
        default void onSuccess() {
        }

        /**
         * @see com.intellij.openapi.progress.Task#onFinished()
         */
        default void onFinished() {
        }

        /**
         * @see com.intellij.openapi.progress.Task#onThrowable(Throwable)
         */
        default void onThrowable(@NotNull Throwable error) {
        }
    }
}
