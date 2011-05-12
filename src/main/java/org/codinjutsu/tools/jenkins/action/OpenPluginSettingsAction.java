package org.codinjutsu.tools.jenkins.action;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.JenkinsControlComponent;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

public class OpenPluginSettingsAction extends AnAction {


    public OpenPluginSettingsAction() {
        super("Jenkins Settings", "Edit the Jenkins settings for the current project", GuiUtil.loadIcon("pluginSettings.png"));
    }

    @Override
  public void actionPerformed(AnActionEvent e) {
    showSettingsFor(getProject(e.getDataContext()));
  }

  protected static void showSettingsFor(Project project) {
    ShowSettingsUtil.getInstance().showSettingsDialog(project, JenkinsControlComponent.JENKINS_CONTROL_COMPONENT_NAME);
  }

    private static Project getProject(DataContext dataContext) {
        return DataKeys.PROJECT.getData(dataContext);
    }
}
