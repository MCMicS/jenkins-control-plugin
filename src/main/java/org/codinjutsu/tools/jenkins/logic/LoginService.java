package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.NotNull;


public class LoginService {

    private static final Logger logger = Logger.getLogger(LoginService.class);
    private final Project project;
    private final SuccessfulAuthenticationNotifier publisher;
    private final RequestManager requestManager;
    private final JenkinsSettings jenkinsSettings;

    public LoginService(final Project project) {
        this.project = project;
        final MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        publisher = myBus.syncPublisher(SuccessfulAuthenticationNotifier.USER_LOGGED_IN);
        requestManager = RequestManager.getInstance(project);
        jenkinsSettings = JenkinsSettings.getSafeInstance(project);
    }

    public void performAuthentication() {
        final JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);

        if (!settings.isServerUrlSet()) {
            logger.warn("Jenkins server is not setup, authentication will not happen");
            return;
        }
        GuiUtil.runInSwingThread(new Task.Backgroundable(project, "Authenticating jenkins", false, JenkinsLoadingTaskOption.INSTANCE) {

            private Jenkins jenkinsWorkspace;

            @Override
            public void onSuccess() {
//                    jenkins.update(jenkinsWorkspace); rememver to add that


                publisher.afterLogin(jenkinsWorkspace);
            }

            @Override
            public void onCancel() {
//                    jobTree.getEmptyText().setText(UNAVAILABLE);
                publisher.loginCancelled();
            }

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                requestManager.authenticate(settings, jenkinsSettings);
                jenkinsWorkspace = requestManager.loadJenkinsWorkspace(settings);
            }
        });

    }

    public static LoginService getInstance(Project project) {
        return ServiceManager.getService(project, LoginService.class);
    }
}
