package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.codinjutsu.tools.jenkins.logic.BrowserPanelAuthenticationHandler;
import org.codinjutsu.tools.jenkins.logic.LoginService;
import org.codinjutsu.tools.jenkins.logic.RssAuthenticationActionHandler;
import org.codinjutsu.tools.jenkins.logic.RssLogic;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

public class StartupJenkinsService implements StartupActivity {


    @Override
    public void runActivity(@NotNull Project project) {
        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        final RssLogic rssLogic = RssLogic.getInstance(project);
        RssAuthenticationActionHandler.getInstance(project);
        BrowserPanelAuthenticationHandler.getInstance(project);
        browserPanel.init();
        rssLogic.init();
        LoginService.getInstance(project).performAuthentication();
    }
}
