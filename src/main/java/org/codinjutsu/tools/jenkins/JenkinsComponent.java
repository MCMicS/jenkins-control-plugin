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

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.view.ConfigurationPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public class JenkinsComponent implements ProjectComponent, Configurable {


    private static final String JENKINS_CONTROL_PLUGIN_NAME = "Jenkins Plugin";
    private static final String JENKINS_CONTROL_COMPONENT_NAME = "JenkinsComponent";

    private final JenkinsAppSettings jenkinsAppSettings;
    private final JenkinsSettings jenkinsSettings;

    private final Project project;

    private ConfigurationPanel configurationPanel;

    public JenkinsComponent(Project project) {
        this.project = project;
        this.jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        this.jenkinsSettings = JenkinsSettings.getSafeInstance(project);
    }


    public void projectOpened() {
        JenkinsWindowManager.getInstance(project);
    }


    public void projectClosed() {
        JenkinsWindowManager.getInstance(project).unregisterMyself();
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
                JenkinsWindowManager.getInstance(project).reloadConfiguration();
            } catch (org.codinjutsu.tools.jenkins.exception.ConfigurationException ex) {
                throw new ConfigurationException(ex.getMessage());
            }
        }
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
