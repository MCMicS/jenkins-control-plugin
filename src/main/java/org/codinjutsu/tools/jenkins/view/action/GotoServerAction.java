package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.text.StringUtil;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

public class GotoServerAction extends AbstractGotoWebPageAction {

    public GotoServerAction(BrowserPanel browserPanel) {
        super("Go to server page", "Open the jenkins server in a web browser", browserPanel);
    }


    @NotNull
    @Override
    protected String getUrl() {
        return browserPanel.getSelectedServer().map(Jenkins::getServerUrl).orElse("");
    }


    @Override
    public void update(AnActionEvent event) {
        final var selectedServer = browserPanel.getSelectedServer();
        final var isServerSelected = selectedServer.isPresent();
        event.getPresentation().setVisible(isServerSelected && !StringUtil.equals(browserPanel.getJenkins().getServerUrl(),
                JenkinsAppSettings.DUMMY_JENKINS_SERVER_URL));
    }
}
