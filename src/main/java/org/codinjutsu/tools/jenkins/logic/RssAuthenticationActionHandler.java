package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.codinjutsu.tools.jenkins.model.Jenkins;

public class RssAuthenticationActionHandler implements AuthenticationNotifier {
    private final Project project;

    public RssAuthenticationActionHandler(final Project project) {
        this.project = project;
        MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        myBus.connect().subscribe(AuthenticationNotifier.USER_LOGGED_IN, this);
    }

    public static RssAuthenticationActionHandler getInstance(Project project) {
        return ServiceManager.getService(project, RssAuthenticationActionHandler.class);
    }

    @Override
    public void emptyConfiguration() {

    }

    @Override
    public void afterLogin(Jenkins jenkinsWorkspace) {
        RssLogic rssLogic = RssLogic.getInstance(project);
        rssLogic.initScheduledJobs();
    }

    @Override
    public void loginCancelled() {
        //nothing to do
    }

}
