/*
 * Copyright (c) 2012 David Boissier
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
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsComponent;
import org.codinjutsu.tools.jenkins.logic.BrowserLogic;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.BuildParamDialog;

import javax.swing.*;

public class RunBuildAction extends AnAction implements DumbAware {

    private static final Icon EXECUTE_ICON = GuiUtil.isUnderDarcula() ? GuiUtil.loadIcon("execute_dark.png") : GuiUtil.loadIcon("execute.png");
    private static final Logger LOG = Logger.getLogger(RunBuildAction.class.getName());

    private final BrowserLogic browserLogic;


    public RunBuildAction(BrowserLogic browserLogic) {
        super("Build on Jenkins", "Run a build on Jenkins Server", EXECUTE_ICON);
        this.browserLogic = browserLogic;
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = ActionUtil.getProject(event);

        final JenkinsComponent jenkinsComponent = project.getComponent(JenkinsComponent.class);
        try {

            Job job = browserLogic.getSelectedJob();

            RequestManager requestManager = browserLogic.getJenkinsManager();
            if (job.hasParameters()) {
                BuildParamDialog.showDialog(job, JenkinsAppSettings.getSafeInstance(project), requestManager, new BuildParamDialog.RunBuildCallback() {

                    public void notifyOnOk(Job job) {
                        notifyOnGoingMessage(jenkinsComponent, job);
                    }

                    public void notifyOnError(Job job, Exception ex) {
                        jenkinsComponent.notifyErrorJenkinsToolWindow("Build '" + job.getName() + "' cannot be run: " + ex.getMessage());
                    }
                });
            } else {
                requestManager.runBuild(job, JenkinsAppSettings.getSafeInstance(project));
                notifyOnGoingMessage(jenkinsComponent, job);
                browserLogic.loadSelectedJob();
            }

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            jenkinsComponent.notifyErrorJenkinsToolWindow("Build cannot be run: " + ex.getMessage());
        }
    }

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = browserLogic.getSelectedJob();
        event.getPresentation().setVisible(selectedJob != null && selectedJob.isBuildable());
    }


    private static void notifyOnGoingMessage(JenkinsComponent jenkinsComponent, Job job) {
        jenkinsComponent.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                job.getName() + " build is on going",
                job.getUrl()));
    }
}
