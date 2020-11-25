package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.ui.AppUIUtil;
import org.codinjutsu.tools.jenkins.logic.BrowserPanelAuthenticationHandler;
import org.codinjutsu.tools.jenkins.logic.LoginService;
import org.codinjutsu.tools.jenkins.logic.RssAuthenticationActionHandler;
import org.jetbrains.annotations.NotNull;

public class StartupJenkinsService implements StartupActivity {
    private static final Logger LOG = Logger.getInstance(JenkinsTree.class);

    @Override
    public void runActivity(@NotNull Project project) {
        RssAuthenticationActionHandler.getInstance(project).subscribe();
        BrowserPanelAuthenticationHandler.getInstance(project).subscribe();
        final LoginService loginService = LoginService.getInstance(project);
        AppUIUtil.invokeLaterIfProjectAlive(project, loginService::performAuthentication);
    }
}
