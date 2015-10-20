/**
 * Created by marcin on 06.10.15.
 */
package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.codinjutsu.tools.jenkins.model.Jenkins;

public class RssAuthenticationActionHandler {
    public RssAuthenticationActionHandler(final Project project) {
        MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        myBus.connect().subscribe(SuccessfulAuthenticationNotifier.USER_LOGGED_IN, new SuccessfulAuthenticationNotifier() {

            @Override
            public void afterLogin(Jenkins jenkinsWorkspace) {
                RssLogic rssLogic = RssLogic.getInstance(project);
                rssLogic.loadLatestBuilds(false);//FIXME is this what should be called
                rssLogic.initScheduledJobs();
            }

            @Override
            public void loginCancelled() {
                //nothing to do
            }
        });
    }

    public static RssAuthenticationActionHandler getInstance(Project project) {
        return ServiceManager.getService(project, RssAuthenticationActionHandler.class);
    }

}
