/*
 * Copyright (c) 2011 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsControlComponent;
import org.codinjutsu.tools.jenkins.logic.JenkinsBrowserLogic;
import org.codinjutsu.tools.jenkins.logic.JenkinsRequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.BuildParamDialog;

public class RunBuildAction extends AnAction {

    private static final Logger LOG = Logger.getLogger(RunBuildAction.class.getName());
    private final JenkinsBrowserLogic jenkinsBrowserLogic;


    public RunBuildAction(JenkinsBrowserLogic jenkinsBrowserLogic) {
        super("Build on Jenkins", "Run a build on Jenkins Server", GuiUtil.loadIcon("cog_go.png"));
        this.jenkinsBrowserLogic = jenkinsBrowserLogic;
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = getProject(event);

        final JenkinsControlComponent jenkinsControlComponent = project.getComponent(JenkinsControlComponent.class);
        try {

            Job job = jenkinsBrowserLogic.getSelectedJob();

            JenkinsRequestManager jenkinsManager = jenkinsBrowserLogic.getJenkinsManager();
            if (job.hasParameters()) {
                BuildParamDialog.showDialog(job, jenkinsControlComponent.getState(), jenkinsManager, new BuildParamDialog.BuildCallback() {

                    public void notifyOnOk(Job job) {
                        jenkinsControlComponent.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                                job.getName() + " build is on going",
                                job.getUrl()), GuiUtil.loadIcon("toolWindowRun.png"));
                    }

                    public void notifyOnError(Job job, Exception ex) {
                        jenkinsControlComponent.notifyErrorJenkinsToolWindow("Build cannot be run: " + ex.getMessage());
                    }
                });
            } else {
                jenkinsManager.runBuild(job, jenkinsControlComponent.getState());

                jenkinsControlComponent.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                        job.getName() + " build is on going",
                        job.getUrl()), GuiUtil.loadIcon("toolWindowRun.png"));
            }

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            jenkinsControlComponent.notifyErrorJenkinsToolWindow("Build cannot be run: " + ex.getMessage());
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
