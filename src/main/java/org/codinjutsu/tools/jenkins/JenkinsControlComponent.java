package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.codinjutsu.tools.jenkins.logic.AuthenticationResult;
import org.codinjutsu.tools.jenkins.logic.DefaultJenkinsRequestManager;
import org.codinjutsu.tools.jenkins.logic.IdeaJenkinsBrowserLogic;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.JenkinsConfigurationPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@State(
        name = JenkinsControlComponent.JENKINS_CONTROL_COMPONENT_NAME,
        storages = {@Storage(id = "JenkinsControlSettings", file = "$PROJECT_FILE$")}
)
public class JenkinsControlComponent
        implements ProjectComponent, Configurable, PersistentStateComponent<JenkinsConfiguration> {

    public static final String JENKINS_CONTROL_COMPONENT_NAME = "JenkinsControlComponent";
    static final String JENKINS_CONTROL_PLUGIN_NAME = "Jenkins Control Plugin";

    private static final String JENKINS_BROWSER = "jenkinsBrowser";
    private static final String JENKINS_BROWSER_TITLE = "Jenkins Browser";
    private static final String JENKINS_BROWSER_ICON = "jenkins_logo_16x16.png";

    private JenkinsConfiguration configuration;
    private JenkinsConfigurationPanel configurationPanel;

    private Project project;
    private IdeaJenkinsBrowserLogic jenkinsBrowserLogic;
    private DefaultJenkinsRequestManager jenkinsRequestManager;


    public JenkinsControlComponent(Project project) {
        this.project = project;
        this.configuration = new JenkinsConfiguration();
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


    public void projectOpened() {
        jenkinsRequestManager = new DefaultJenkinsRequestManager();
        installJenkinsBrowser();
    }


    private void installJenkinsBrowser() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow(JENKINS_BROWSER,
                true,
                ToolWindowAnchor.RIGHT);

        jenkinsBrowserLogic = new IdeaJenkinsBrowserLogic(configuration, jenkinsRequestManager);
        jenkinsBrowserLogic.init();


        JPanel yourContentPanel = jenkinsBrowserLogic.getView();
        Content content = ContentFactory.SERVICE.getInstance()
                .createContent(yourContentPanel, JENKINS_BROWSER_TITLE, false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.setIcon(GuiUtil.loadIcon(JENKINS_BROWSER_ICON));
    }


    public void projectClosed() {
        ToolWindowManager.getInstance(project).unregisterToolWindow(JENKINS_BROWSER);
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


    public void reset() {
        configurationPanel.loadConfigurationData(configuration);
    }


    public void notifyInfoJenkinsToolWindow(String message, Icon icon) {
        ToolWindowManager.getInstance(project).notifyByBalloon(JENKINS_BROWSER, MessageType.INFO,
                message, icon, new BrowserHyperlinkListener());
    }

    public void notifyErrorJenkinsToolWindow(String message) {
        ToolWindowManager.getInstance(project).notifyByBalloon(JENKINS_BROWSER, MessageType.ERROR,
                message);
    }


    public void initComponent() {

    }


    public void disposeComponent() {
    }
}
