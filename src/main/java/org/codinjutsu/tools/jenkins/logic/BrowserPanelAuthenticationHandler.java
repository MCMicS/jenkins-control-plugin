package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

public class BrowserPanelAuthenticationHandler implements AuthenticationNotifier {

    private final BrowserPanel browser;

    public BrowserPanelAuthenticationHandler(final Project project ) {
        final MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        browser = BrowserPanel.getInstance(project);
        myBus.connect().subscribe(AuthenticationNotifier.USER_LOGGED_IN, this);
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
        browser.setJobsUnavailable();
    }
}
