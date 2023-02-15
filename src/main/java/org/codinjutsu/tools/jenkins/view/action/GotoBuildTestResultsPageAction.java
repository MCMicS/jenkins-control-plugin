package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

public class GotoBuildTestResultsPageAction extends AbstractGotoWebPageAction {

    public GotoBuildTestResultsPageAction(BrowserPanel browserPanel) {
        super("Go to the build test results page", "Open the build test results page in a web browser", browserPanel);
    }


    @NotNull
    @Override
    protected String getUrl() {
        return browserPanel.getSelectedBuildUrl() + "/testReport";
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(browserPanel.getSelectedBuild().isPresent());
    }
}
