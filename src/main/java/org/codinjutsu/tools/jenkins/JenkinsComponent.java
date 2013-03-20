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

package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.*;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.codinjutsu.tools.jenkins.logic.RssLogic;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.ConfigurationPanel;
import org.codinjutsu.tools.jenkins.view.JenkinsWidget;
import org.codinjutsu.tools.jenkins.view.action.RefreshRssAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public class JenkinsComponent implements ProjectComponent, Configurable {

    private static final Icon JENKINS_ICON = GuiUtil.loadIcon("jenkins_logo.png");

    private static final String JENKINS_CONTROL_PLUGIN_NAME = "Jenkins Plugin";

    static final String JENKINS_CONTROL_COMPONENT_NAME = "JenkinsComponent";

    public static final String JENKINS_BROWSER = "Jenkins";

    private final JenkinsAppSettings jenkinsAppSettings;
    private final JenkinsSettings jenkinsSettings;

    private ConfigurationPanel configurationPanel;

    private final Project project;

    public JenkinsComponent(Project project) {
        this.project = project;
        this.jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        this.jenkinsSettings = JenkinsSettings.getSafeInstance(project);
    }


    public void projectOpened() {

        JenkinsWidget jenkinsWidget = JenkinsWidget.getInstance(project);

        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);

        final RssLogic rssLogic = RssLogic.getInstance(project);

        StartupManager.getInstance(project).registerPostStartupActivity(new DumbAwareRunnable() {
            @Override
            public void run() {
                browserPanel.init(new RefreshRssAction(rssLogic));
                rssLogic.init();

                browserPanel.initScheduledJobs();
                rssLogic.initScheduledJobs();
            }
        });

        final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        statusBar.addWidget(jenkinsWidget);
        jenkinsWidget.install(statusBar);

        Content content = ContentFactory.SERVICE.getInstance().createContent(browserPanel, null, false);
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow(JENKINS_BROWSER, false, ToolWindowAnchor.RIGHT);
        toolWindow.setIcon(JENKINS_ICON);
        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.addContent(content);
    }


    public void projectClosed() {
        ToolWindowManager.getInstance(project).unregisterToolWindow(JENKINS_BROWSER);
        BrowserPanel.getInstance(project).dispose();
        RssLogic.getInstance(project).dispose();
        JenkinsWidget.getInstance(project).dispose();
    }


    public JComponent createComponent() {
        if (configurationPanel == null) {
            configurationPanel = new ConfigurationPanel(project);
        }
        return configurationPanel.getRootPanel();
    }


    public boolean isModified() {
        return configurationPanel != null && configurationPanel.isModified(jenkinsAppSettings, jenkinsSettings);
    }


    public void disposeUIResources() {
        configurationPanel = null;
    }

    public String getHelpTopic() {
        return null;
    }


    public void apply() throws ConfigurationException {
        if (configurationPanel != null) {
            try {
                configurationPanel.applyConfigurationData(jenkinsAppSettings, jenkinsSettings);
                BrowserPanel.getInstance(project).reloadConfiguration();

            } catch (org.codinjutsu.tools.jenkins.exception.ConfigurationException ex) {
                throw new ConfigurationException(ex.getMessage());
            }
        }
    }


    public void notifyInfoJenkinsToolWindow(final String message) {
        ToolWindowManager.getInstance(project).notifyByBalloon(
                JENKINS_BROWSER,
                MessageType.INFO,
                message,
                null,
                new BrowserHyperlinkListener());
    }


    public void notifyErrorJenkinsToolWindow(final String message) {
        ToolWindowManager.getInstance(project).notifyByBalloon(JENKINS_BROWSER, MessageType.ERROR, message);
    }


    @NotNull
    public String getComponentName() {
        return JENKINS_CONTROL_COMPONENT_NAME;
    }


    @Nls
    public String getDisplayName() {
        return JENKINS_CONTROL_PLUGIN_NAME;
    }


    public Icon getIcon() {
        return null;
    }


    public void reset() {
        configurationPanel.loadConfigurationData(jenkinsAppSettings, jenkinsSettings);
    }


    public void initComponent() {

    }


    public void disposeComponent() {

    }
}
