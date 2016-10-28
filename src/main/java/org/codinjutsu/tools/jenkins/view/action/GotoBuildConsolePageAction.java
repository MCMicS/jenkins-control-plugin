package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

public class GotoBuildConsolePageAction extends AbstractGotoWebPageAction {

    public GotoBuildConsolePageAction(BrowserPanel browserPanel) {
        super("Go to the build console page", "Open the build console page in a web browser", browserPanel);
    }


    @Override
    protected String getUrl() {
        return browserPanel.getSelectedBuild().getUrl() + "/console";
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(browserPanel.getSelectedBuild() != null);
    }
}
