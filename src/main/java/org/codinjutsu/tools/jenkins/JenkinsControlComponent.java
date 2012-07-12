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

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.*;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.codinjutsu.tools.jenkins.logic.BuildStatusAggregator;
import org.codinjutsu.tools.jenkins.logic.JenkinsBrowserLogic;
import org.codinjutsu.tools.jenkins.logic.JenkinsRequestManager;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.TimeUnit;

import static org.codinjutsu.tools.jenkins.view.action.RunBuildAction.RUN_ICON;

@State(
        name = JenkinsControlComponent.JENKINS_CONTROL_COMPONENT_NAME,
        storages = {@Storage(id = "JenkinsControlSettings", file = "$PROJECT_FILE$")}
)
public class JenkinsControlComponent
        implements ProjectComponent, Configurable, PersistentStateComponent<JenkinsConfiguration> {

    static final String JENKINS_CONTROL_COMPONENT_NAME = "JenkinsControlComponent";
    private static final String JENKINS_CONTROL_PLUGIN_NAME = "Jenkins Control Plugin";

    private static final String JENKINS_BROWSER = "jenkinsBrowser";
    private static final String JENKINS_BROWSER_TITLE = "Jenkins Browser";
    private static final String JENKINS_BROWSER_ICON = "jenkins_logo.png";

    private final JenkinsConfiguration configuration;
    private JenkinsConfigurationPanel configurationPanel;

    private final Project project;
    private JenkinsBrowserLogic jenkinsBrowserLogic;
    private JenkinsRequestManager jenkinsRequestManager;
    private JenkinsWidget jenkinsWidget;


    public JenkinsControlComponent(Project project) {
        this.project = project;
        this.configuration = new JenkinsConfiguration();
    }


    public void projectOpened() {
        installJenkinsPanel();
    }


    public void projectClosed() {
        jenkinsBrowserLogic.dispose();
        ToolWindowManager.getInstance(project).unregisterToolWindow(JENKINS_BROWSER);
    }


    public JComponent createComponent() {
        if (configurationPanel == null) {
            configurationPanel = new JenkinsConfigurationPanel(jenkinsRequestManager);
        }
        return configurationPanel.getRootPanel();
    }


    public boolean isModified() {
        return configurationPanel != null && configurationPanel.isModified(configuration);
    }


    public void disposeUIResources() {
        configurationPanel = null;
    }


    public JenkinsConfiguration getState() {
        return configuration;
    }


    public void loadState(JenkinsConfiguration jenkinsConfiguration) {
        XmlSerializerUtil.copyBean(jenkinsConfiguration, configuration);
    }


    public String getHelpTopic() {
        return null;
    }


    public void apply() throws ConfigurationException {
        if (configurationPanel != null) {
            try {
                configurationPanel.applyConfigurationData(configuration);
                jenkinsBrowserLogic.reloadConfiguration();

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
                RUN_ICON,
                new BrowserHyperlinkListener());
    }


    public void notifyErrorJenkinsToolWindow(final String message) {
        ToolWindowManager.getInstance(project).notifyByBalloon(JENKINS_BROWSER, MessageType.ERROR, message);
    }


    private void installJenkinsPanel() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow(JENKINS_BROWSER, false, ToolWindowAnchor.RIGHT);

        jenkinsRequestManager = new JenkinsRequestManager(configuration.getCrumbFile());

        JenkinsBrowserPanel browserPanel = new JenkinsBrowserPanel();
        RssLatestBuildPanel rssLatestJobPanel = new RssLatestBuildPanel();

        jenkinsWidget = new JenkinsWidget();

        JenkinsBrowserLogic.BuildStatusListener buildStatusListener = new JenkinsBrowserLogic.BuildStatusListener() {
            public void onBuildFailure(final String jobName, final Build build) {
                BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(jobName + "#" + build.getNumber() + ": FAILED", MessageType.ERROR, null);
                Balloon balloon = balloonBuilder.setFadeoutTime(TimeUnit.SECONDS.toMillis(1)).createBalloon();
                balloon.show(new RelativePoint(jenkinsWidget.getComponent(), new Point(0, 0)), Balloon.Position.above);
            }
        };

        JenkinsBrowserLogic.JobLoadListener jobLoadListener = new JenkinsBrowserLogic.JobLoadListener() {
            @Override
            public void afterLoadingJobs(BuildStatusAggregator buildStatusAggregator) {
                jenkinsWidget.updateInformation(buildStatusAggregator);
            }
        };

        configuration.getBrowserPreferences().setLastSelectedView(PropertiesComponent.getInstance(project).getValue("last_selected_view"));
        jenkinsBrowserLogic = new JenkinsBrowserLogic(configuration, jenkinsRequestManager, browserPanel, rssLatestJobPanel, buildStatusListener, jobLoadListener);
        jenkinsBrowserLogic.getJenkinsBrowserPanel().getViewCombo().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                View selectedView = (View) jenkinsBrowserLogic.getJenkinsBrowserPanel().getViewCombo().getSelectedItem();
                PropertiesComponent.getInstance(project).setValue("last_selected_view", selectedView.getName());
            }
        });

        StartupManager.getInstance(project).registerPostStartupActivity(new DumbAwareRunnable() {
            @Override
            public void run() {
                jenkinsBrowserLogic.init();
            }
        });

        final StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        statusBar.addWidget(jenkinsWidget);
        jenkinsWidget.install(statusBar);


        Content content = ContentFactory.SERVICE.getInstance()
                .createContent(JenkinsPanel.onePanel(browserPanel, rssLatestJobPanel), JENKINS_BROWSER_TITLE, false);
        toolWindow.getContentManager().

                addContent(content);

        toolWindow.setIcon(GuiUtil.loadIcon(JENKINS_BROWSER_ICON));
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
        configurationPanel.loadConfigurationData(configuration);
    }


    public void initComponent() {

    }


    public void disposeComponent() {
        jenkinsWidget.dispose();
    }
}
