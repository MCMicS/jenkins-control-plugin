/**
 * Created by marcin on 06.10.15.
 */
package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

public class BrowserPanelAuthenticationHandler {

    public BrowserPanelAuthenticationHandler(final Project project ) {
        final MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        final BrowserPanel browser = BrowserPanel.getInstance(project);
        myBus.connect().subscribe(SuccessfulAuthenticationNotifier.USER_LOGGED_IN, new SuccessfulAuthenticationNotifier() {

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
        });
    }

    public static BrowserPanelAuthenticationHandler getInstance(Project project) {
        return ServiceManager.getService(project, BrowserPanelAuthenticationHandler.class);
    }


}
