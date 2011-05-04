package org.codinjustu.tools.jenkins.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.codinjustu.tools.jenkins.JenkinsControlComponent;
import org.codinjustu.tools.jenkins.logic.IdeaJenkinsBrowserLogic;
import org.codinjustu.tools.jenkins.model.Job;
import org.codinjustu.tools.jenkins.util.GuiUtil;
import org.codinjustu.tools.jenkins.util.HtmlUtil;

import java.io.IOException;

public class JenkinsBuildAction extends AnAction {

    private static final Logger LOG = Logger.getLogger(JenkinsBuildAction.class.getName());
    private IdeaJenkinsBrowserLogic jenkinsBrowserLogic;


    public JenkinsBuildAction(IdeaJenkinsBrowserLogic jenkinsBrowserLogic) {
        super("Build on Jenkins", "Run a build on Jenkins Server", GuiUtil.loadIcon("cog_go.png"));
        this.jenkinsBrowserLogic = jenkinsBrowserLogic;
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        try {
            Project project = getProject(event);

            JenkinsControlComponent jenkinsControlComponent
                    = project.getComponent(JenkinsControlComponent.class);

            Job job = jenkinsBrowserLogic.getSelectedJob();
            jenkinsBrowserLogic.getJenkinsManager().runBuild(job, jenkinsControlComponent.getState());

            jenkinsControlComponent.notifyJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                    job.getName() + " build is on going",
                    job.getUrl()), GuiUtil.loadIcon("toolWindowRun.png"));
        } catch (IOException ioEx) {
            LOG.error(ioEx.getMessage(), ioEx);
            GuiUtil.showErrorDialog(ioEx.getMessage(), "Error during executing the following request");
        }
    }


    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setVisible(jenkinsBrowserLogic.getSelectedJob() != null);
    }


    private static Project getProject(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        return DataKeys.PROJECT.getData(dataContext);
    }
}
