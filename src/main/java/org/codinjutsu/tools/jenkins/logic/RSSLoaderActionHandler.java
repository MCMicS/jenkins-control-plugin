/**
 * Created by marcin on 06.10.15.
 */
package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;

public class RSSLoaderActionHandler {
    public RSSLoaderActionHandler() {
        MessageBus myBus = ApplicationManager.getApplication().getMessageBus();
        myBus.connect().subscribe(RefreshActionNotifier.USER_LOGGED_IN, new RefreshActionNotifier() {

            @Override
            public void afterLogin(Project project) {
                RssLogic rssLogic = RssLogic.getInstance(project);
                rssLogic.loadLatestBuilds(false);
            }
        });
    }

    public static RSSLoaderActionHandler getInstance(Project project) {
        return ServiceManager.getService(project, RSSLoaderActionHandler.class);
    }

}
