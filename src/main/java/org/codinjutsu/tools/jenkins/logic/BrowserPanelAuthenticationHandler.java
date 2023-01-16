package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

public class BrowserPanelAuthenticationHandler implements AuthenticationNotifier, Disposable {

    private final BrowserPanel browser;
    private final MessageBusConnection connection;

    public BrowserPanelAuthenticationHandler(final Project project ) {
        browser = BrowserPanel.getInstance(project);
        connection = ApplicationManager.getApplication().getMessageBus().connect();
    }

    public void subscribe() {
        connection.subscribe(AuthenticationNotifier.USER_LOGGED_IN, this);
    }

    public static BrowserPanelAuthenticationHandler getInstance(Project project) {
        return project.getService(BrowserPanelAuthenticationHandler.class);
    }

    @Override
    public void emptyConfiguration(){
        GuiUtil.runInSwingThread(browser::handleEmptyConfiguration);
    }

    @Override
    public void afterLogin(Jenkins jenkinsWorkspace) {
        GuiUtil.runInSwingThread(() -> {
            browser.updateWorkspace(jenkinsWorkspace);
            browser.postAuthenticationInitialization();
            browser.initScheduledJobs();
        });
    }

    @Override
    public void loginCancelled() {
        emptyConfiguration();
    }

    @Override
    public void loginFailed(Throwable ex) {
        final String message = ex.getLocalizedMessage() == null ? "Unknown error" : ex.getLocalizedMessage();
        browser.notifyErrorJenkinsToolWindow(message);
    }

    @Override
    public void dispose() {
        connection.disconnect();
    }
}
