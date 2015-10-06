/**
 * Created by marcin on 06.10.15.
 */

package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;

public class UserLoggedIn {
    public UserLoggedIn(Project project) {
        MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        LoginActionNotifier publisher = myBus.syncPublisher(LoginActionNotifier.USER_LOGGED_IN);
        publisher.afterLogin(project);
    }


}
