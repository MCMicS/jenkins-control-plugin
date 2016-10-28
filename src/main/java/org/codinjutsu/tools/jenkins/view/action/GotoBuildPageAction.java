package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

public class GotoBuildPageAction extends AbstractGotoWebPageAction {

    public GotoBuildPageAction(BrowserPanel browserPanel) {
        super("Go to the build page", "Open the build page in a web browser", browserPanel);
    }


    @Override
    protected String getUrl() {
        return browserPanel.getSelectedBuild().getUrl();
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(browserPanel.getSelectedBuild() != null);
    }
}
