package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.codinjutsu.tools.jenkins.model.Jenkins;

public class RssAuthenticationActionHandler implements AuthenticationNotifier, Disposable {
    private final Project project;
    private final MessageBusConnection connection;

    public RssAuthenticationActionHandler(final Project project) {
        this.project = project;
        connection = ApplicationManager.getApplication().getMessageBus().connect();
    }

    public void subscribe() {
        connection.subscribe(AuthenticationNotifier.USER_LOGGED_IN, this);
    }

    public static RssAuthenticationActionHandler getInstance(Project project) {
        return ServiceManager.getService(project, RssAuthenticationActionHandler.class);
    }

    @Override
    public void emptyConfiguration() {
        // nothing to do
    }

    @Override
    public void afterLogin(Jenkins jenkinsWorkspace) {
        RssLogic rssLogic = RssLogic.getInstance(project);
        rssLogic.initScheduledJobs();
    }

    @Override
    public void loginCancelled() {
        //nothing to do
    }

    @Override
    public void loginFailed(Exception ex) {
        //nothing to do
    }

    @Override
    public void dispose() {
        connection.disconnect();
    }
}
