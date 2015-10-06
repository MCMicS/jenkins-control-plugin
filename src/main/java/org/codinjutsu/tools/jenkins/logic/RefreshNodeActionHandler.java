/**
 * Created by marcin on 06.10.15.
 */
package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

public class RefreshNodeActionHandler {
    public RefreshNodeActionHandler() {
        MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        myBus.connect().subscribe(LoginActionNotifier.USER_LOGGED_IN, new LoginActionNotifier() {

            @Override
            public void afterLogin(Project project) {
                BrowserPanel browser = BrowserPanel.getInstance(project);
                browser.refreshCurrentView();
            }
        });
    }

    public static RefreshNodeActionHandler getInstance(Project project) {
        return ServiceManager.getService(project, RefreshNodeActionHandler.class);
    }


}
