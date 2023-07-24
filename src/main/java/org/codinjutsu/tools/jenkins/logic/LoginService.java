package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.jetbrains.annotations.NotNull;


public class LoginService {

    private static final Logger logger = Logger.getInstance(LoginService.class);
    private final Project project;

    public LoginService(final Project project) {
        this.project = project;
    }

    public void performAuthentication() {
        if(!ApplicationManager.getApplication().isDispatchThread()){
            logger.warn("LoginService.performAuthentication called from outside of EDT");
        }
        final JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);
        final AuthenticationNotifier publisher = ApplicationManager.getApplication().getMessageBus()
                .syncPublisher(AuthenticationNotifier.USER_LOGGED_IN);
        if (!settings.isServerUrlSet()) {
            logger.warn("Jenkins server is not setup, authentication will not happen");
            publisher.emptyConfiguration();
            return;
        }
        final JenkinsSettings jenkinsSettings = JenkinsSettings.getSafeInstance(project);
        JenkinsBackgroundTaskFactory.getInstance(project).createBackgroundTask("Authenticating jenkins",
                new JenkinsBackgroundTask.JenkinsTask() {

                    private Jenkins jenkinsWorkspace;

                    @Override
                    public void run(@NotNull RequestManagerInterface requestManager) {
                        try {
                            requestManager.authenticate(settings, jenkinsSettings);
                            jenkinsWorkspace = requestManager.loadJenkinsWorkspace(settings, jenkinsSettings);
                        } catch (Exception ex) {
                            publisher.loginCancelled();
                            throw ex;
                        }
                    }

                    @Override
                    public void onSuccess() {
                        JenkinsBackgroundTask.JenkinsTask.super.onSuccess();
                        publisher.afterLogin(jenkinsWorkspace);
                    }

                    @Override
                    public void onCancel() {
                        JenkinsBackgroundTask.JenkinsTask.super.onCancel();
                        publisher.loginCancelled();
                    }

                    @Override
                    public void onThrowable(Throwable error) {
                        JenkinsBackgroundTask.JenkinsTask.super.onThrowable(error);
                        publisher.loginFailed(error);
                    }
                }).queue();
    }

    public static LoginService getInstance(Project project) {
        return project.getService(LoginService.class);
    }
}
