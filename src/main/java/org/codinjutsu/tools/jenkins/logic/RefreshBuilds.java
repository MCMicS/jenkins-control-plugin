/**
 * Created by marcin on 06.10.15.
 */

package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;

public class RefreshBuilds {
    public RefreshBuilds(Project project) {
        MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        RefreshActionNotifier publisher = myBus.syncPublisher(RefreshActionNotifier.USER_LOGGED_IN);
        publisher.afterLogin(project);
    }


}
