/**
 * Created by marcin on 06.10.15.
 */

package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;

//FIXME don't really like that it does on in constructor, probably this class could be removed
public class RefreshBuilds {

    @Deprecated
    public RefreshBuilds(Project project) {
        MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        SuccessfulAuthenticationNotifier publisher = myBus.syncPublisher(SuccessfulAuthenticationNotifier.USER_LOGGED_IN);
//        publisher.afterLogin(project);
    }


}
