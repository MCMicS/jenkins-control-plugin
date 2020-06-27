package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

public class BrowserPanelAuthenticationHandler implements AuthenticationNotifier, Disposable {

    private final BrowserPanel browser;
    private final MessageBusConnection connection;

    public BrowserPanelAuthenticationHandler(final Project project ) {
        browser = BrowserPanel.getInstance(project);
        final MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        connection = myBus.connect();
    }

    public void subscribe() {
        connection.subscribe(AuthenticationNotifier.USER_LOGGED_IN, this);
    }

    public static BrowserPanelAuthenticationHandler getInstance(Project project) {
        return ServiceManager.getService(project, BrowserPanelAuthenticationHandler.class);
    }

    @Override
    public void emptyConfiguration(){
        browser.handleEmptyConfiguration();
    }

    @Override
    public void afterLogin(Jenkins jenkinsWorkspace) {
        browser.updateWorkspace(jenkinsWorkspace);
        browser.postAuthenticationInitialization();
        browser.initScheduledJobs();
    }

    @Override
    public void loginCancelled() {
        browser.handleEmptyConfiguration();
    }

    @Override
    public void loginFailed(Exception ex) {
        final String message = ex.getLocalizedMessage() == null ? "Unknown error" : ex.getLocalizedMessage();
        browser.notifyErrorJenkinsToolWindow(message);
    }

    @Override
    public void dispose() {
        connection.disconnect();
    }
}
