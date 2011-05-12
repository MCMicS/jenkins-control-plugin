package org.codinjutsu.tools.jenkins.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserView;

public class GotoLastBuildPageAction extends AbstractGotoWebPageAction {

    public GotoLastBuildPageAction(JenkinsBrowserView jenkinsBrowserPanel) {
        super("Go to the latest build page",
                "Open the latest build page in a web browser",
                "page_gear.png", jenkinsBrowserPanel);
    }


    @Override
    public String getUrl() {
        Job job = jenkinsBrowserPanel.getSelectedJob();
        return job.getLastBuild().getUrl();
    }


    @Override
    public void update(AnActionEvent event) {
        Job job = jenkinsBrowserPanel.getSelectedJob();
        event.getPresentation().setVisible(job != null
                && job.getLastBuild() != null
                && !job.isInQueue());
    }
}