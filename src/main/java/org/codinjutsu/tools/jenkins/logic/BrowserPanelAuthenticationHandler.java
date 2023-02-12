package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

import java.util.function.Consumer;

public class BrowserPanelAuthenticationHandler implements AuthenticationNotifier, Disposable {

    private final Project project;
    private final MessageBusConnection connection;

    public BrowserPanelAuthenticationHandler(final Project project) {
        this.project = project;
        this.connection = ApplicationManager.getApplication().getMessageBus().connect();
    }

    public static BrowserPanelAuthenticationHandler getInstance(Project project) {
        return project.getService(BrowserPanelAuthenticationHandler.class);
    }

    public void subscribe() {
        connection.subscribe(AuthenticationNotifier.USER_LOGGED_IN, this);
    }

    @Override
    public void emptyConfiguration() {
        executeForCurrentProject(BrowserPanel::handleEmptyConfiguration);
    }

    @Override
    public void afterLogin(Jenkins jenkinsWorkspace) {
        executeForCurrentProject((BrowserPanel browser) -> {
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
        executeForCurrentProject(browser -> browser.notifyErrorJenkinsToolWindow(message));
    }

    private void executeForCurrentProject(Consumer<BrowserPanel> browserPanelConsumer) {
        GuiUtil.runInSwingThread(() -> browserPanelConsumer.accept(BrowserPanel.getInstance(project)));
    }

    @Override
    public void dispose() {
        connection.disconnect();
    }
}
