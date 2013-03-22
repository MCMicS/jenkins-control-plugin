/*
 * Copyright (c) 2013 David Boissier
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

package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.codinjutsu.tools.jenkins.logic.RssLogic;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.JenkinsWidget;
import org.codinjutsu.tools.jenkins.view.action.RefreshRssAction;

import javax.swing.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class JenkinsWindowManager {

    private static final Icon JENKINS_ICON = GuiUtil.loadIcon("jenkins_logo.png");

    public static final String JENKINS_BROWSER = "Jenkins";
    private final Project project;

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);


    public static JenkinsWindowManager getInstance(Project project) {
        return ServiceManager.getService(project, JenkinsWindowManager.class);
    }

    public JenkinsWindowManager(Project project) {
        this.project = project;

        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);

        Content content = ContentFactory.SERVICE.getInstance().createContent(browserPanel, null, false);
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow(JENKINS_BROWSER, false, ToolWindowAnchor.RIGHT);
        toolWindow.setIcon(JENKINS_ICON);
        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.addContent(content);

        final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JenkinsWidget jenkinsWidget = JenkinsWidget.getInstance(project);
        statusBar.addWidget(jenkinsWidget);
        jenkinsWidget.install(statusBar);

        final RssLogic rssLogic = RssLogic.getInstance(project);

        StartupManager.getInstance(project).registerPostStartupActivity(new DumbAwareRunnable() {
            @Override
            public void run() {

                browserPanel.init();
                browserPanel.initScheduledJobs(scheduledThreadPoolExecutor);
            }
        });
    }

    public void unregisterMyself() {
        ToolWindowManager.getInstance(project).unregisterToolWindow(JenkinsWindowManager.JENKINS_BROWSER);
        BrowserPanel.getInstance(project).dispose();
        JenkinsWidget.getInstance(project).dispose();
        scheduledThreadPoolExecutor.shutdown();
    }

    public void reloadConfiguration() {
        BrowserPanel.getInstance(project).reloadConfiguration();
    }
}
