package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.codinjutsu.tools.jenkins.logic.BrowserPanelAuthenticationHandler;
import org.codinjutsu.tools.jenkins.logic.JenkinsNotifier;
import org.codinjutsu.tools.jenkins.logic.LoginService;
import org.codinjutsu.tools.jenkins.logic.RssAuthenticationActionHandler;
import org.jetbrains.annotations.NotNull;

public class StartupJenkinsService implements StartupActivity, DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        RssAuthenticationActionHandler.getInstance(project).subscribe();
        BrowserPanelAuthenticationHandler.getInstance(project).subscribe();
        ApplicationManager.getApplication().invokeLater(() -> LoginService.getInstance(project).performAuthentication());
        initializeNotification(project);
    }

    private void initializeNotification(@NotNull Project project) {
        JenkinsNotifier.getInstance(project);
    }
}
