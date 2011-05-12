package org.codinjutsu.tools.jenkins.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserView;

public class GotoJobPageAction extends AbstractGotoWebPageAction {

    public GotoJobPageAction(JenkinsBrowserView jenkinsBrowserPanel) {
        super("Go to the job page", "Open the job page in a web browser", "page_go.png", jenkinsBrowserPanel);
    }


    @Override
    protected String getUrl() {
        return jenkinsBrowserPanel.getSelectedJob().getUrl();
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(jenkinsBrowserPanel.getSelectedJob() != null);
    }
}