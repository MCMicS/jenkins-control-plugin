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

package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.JenkinsRequestManager;
import org.codinjutsu.tools.jenkins.security.SecurityMode;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uispec4j.CheckBox;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.UISpecTestCase;

import static org.codinjutsu.tools.jenkins.JenkinsConfiguration.DEFAULT_BUILD_DELAY;
import static org.codinjutsu.tools.jenkins.JenkinsConfiguration.DEFAULT_JENKINS_SERVER_URL;

public class JenkinsConfigurationPanelTest extends UISpecTestCase {

    private JenkinsConfigurationPanel jenkinsConfigurationPanel;
    private JenkinsConfiguration configuration;
    private Panel uiSpecPanel;

    @Mock
    private JenkinsRequestManager jenkinsRequestManager;

    public void testDisplayWithDefaultValues() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.textEquals(DEFAULT_JENKINS_SERVER_URL).check();

        TextBox buildDelayBox = uiSpecPanel.getTextBox("buildDelay");
        buildDelayBox.textEquals(Integer.toString(DEFAULT_BUILD_DELAY)).check();

        CheckBox enableJobCheckBox = uiSpecPanel.getCheckBox("enableJobAutoRefresh");
        assertFalse(enableJobCheckBox.isSelected());

        TextBox jobRefreshPeriodBox = uiSpecPanel.getTextBox("jobRefreshPeriod");
        jobRefreshPeriodBox.textEquals("0").check();
        assertFalse(jobRefreshPeriodBox.isEnabled());

        CheckBox enableRssCheckBox = uiSpecPanel.getCheckBox("enableRssAutoRefresh");
        assertFalse(enableRssCheckBox.isSelected());

        TextBox rssRefreshPeriodBox = uiSpecPanel.getTextBox("rssRefreshPeriod");
        rssRefreshPeriodBox.textEquals("0").check();
        assertFalse(rssRefreshPeriodBox.isEnabled());

        CheckBox enableAuthenticationCheckBox = uiSpecPanel.getCheckBox("enableAuthentication");
        assertFalse(enableAuthenticationCheckBox.isSelected());

        TextBox usernameTextbox = uiSpecPanel.getTextBox("username");
        assertFalse(usernameTextbox.isEnabled());
        usernameTextbox.textIsEmpty().check();

        TextBox passwordTextField = uiSpecPanel.getTextBox("passwordFile");
        assertFalse(passwordTextField.isEnabled());
        passwordTextField.textIsEmpty().check();

        TextBox crumbDataFileTextField = uiSpecPanel.getTextBox("crumbDataFile");
        crumbDataFileTextField.textIsEmpty().check();

    }


    public void testValidationOk() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("http://anotherjenkinsserver:1010/jenkins");

        TextBox buildDelay = uiSpecPanel.getTextBox("buildDelay");
        buildDelay.setText("10");

        TextBox jobRefreshPeriodBox = uiSpecPanel.getTextBox("jobRefreshPeriod");
        CheckBox enableJobCheckBox = uiSpecPanel.getCheckBox("enableJobAutoRefresh");

        assertFalse(jobRefreshPeriodBox.isEnabled());
        enableJobCheckBox.click();
        assertTrue(jobRefreshPeriodBox.isEnabled());
        jobRefreshPeriodBox.setText("2");

        TextBox rssRefreshPeriodBox = uiSpecPanel.getTextBox("rssRefreshPeriod");
        CheckBox enableRssCheckBox = uiSpecPanel.getCheckBox("enableRssAutoRefresh");

        assertFalse(rssRefreshPeriodBox.isEnabled());
        enableRssCheckBox.click();
        assertTrue(rssRefreshPeriodBox.isEnabled());
        rssRefreshPeriodBox.setText("5");
        enableRssCheckBox.click();
        rssRefreshPeriodBox.textEquals("0").check();
        assertFalse(rssRefreshPeriodBox.isEnabled());

        CheckBox enableAuthenticationCheckBox = uiSpecPanel.getCheckBox("enableAuthentication");
        enableAuthenticationCheckBox.click();

        TextBox usernameTextbox = uiSpecPanel.getTextBox("username");
        assertTrue(usernameTextbox.isEnabled());
        usernameTextbox.setText("johndoe");

        TextBox passwordFileField = uiSpecPanel.getTextBox("passwordFile");
        assertTrue(passwordFileField.isEnabled());
        passwordFileField.setText("D:/password.txt");

        TextBox crumbDataFileField = uiSpecPanel.getTextBox("crumbDataFile");
        assertTrue(crumbDataFileField.isEnabled());
        crumbDataFileField.setText("D:/crumbData.txt");

        jenkinsConfigurationPanel.applyConfigurationData(configuration);

        assertEquals("http://anotherjenkinsserver:1010/jenkins", configuration.getServerUrl());
        assertEquals(10, configuration.getBuildDelay());
        assertEquals(2, configuration.getJobRefreshPeriod());
        assertEquals(0, configuration.getRssRefreshPeriod());
        assertTrue(configuration.isEnableJobAutoRefresh());
        assertFalse(configuration.isEnableRssAutoRefresh());
        assertEquals(SecurityMode.BASIC, configuration.getSecurityMode());
        assertEquals("johndoe", configuration.getUsername());
        assertEquals("D:/password.txt", configuration.getPasswordFile());
        assertEquals("D:/crumbData.txt", configuration.getCrumbFile());
    }


    public void testApplyConfigWithEmptyParamValueShouldFail() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("");
        try {
            jenkinsConfigurationPanel.applyConfigurationData(configuration);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("'serverUrl' must be set", ex.getMessage());
        }

        serverUrlBox.setText(null);
        try {
            jenkinsConfigurationPanel.applyConfigurationData(configuration);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("'serverUrl' must be set", ex.getMessage());
        }
    }


    public void testApplyConfigWithMalformedUrlShouldFail() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("portnawak");

        try {
            jenkinsConfigurationPanel.applyConfigurationData(configuration);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("URL 'portnawak' is malformed", ex.getMessage());
        }
    }


    public void testApplyConfigWithInvalidIntegerShouldFail() throws Exception {

        CheckBox enableJobCheckBox = uiSpecPanel.getCheckBox("enableJobAutoRefresh");
        enableJobCheckBox.click();

        TextBox jobRefreshPeriod = uiSpecPanel.getTextBox("jobRefreshPeriod");
        jobRefreshPeriod.setText("portnawak");

        try {
            jenkinsConfigurationPanel.applyConfigurationData(configuration);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("'portnawak' is not a positive integer", ex.getMessage());
        }

        jobRefreshPeriod.setText("0");
        try {
            jenkinsConfigurationPanel.applyConfigurationData(configuration);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("'0' is not a positive integer", ex.getMessage());
        }

        jobRefreshPeriod.setText("-1");
        try {
            jenkinsConfigurationPanel.applyConfigurationData(configuration);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("'-1' is not a positive integer", ex.getMessage());
        }
    }

    public void testApplyAuthenticationWithInvalidUserParameters() throws Exception {
        CheckBox enableAuthenticationCheckBox = uiSpecPanel.getCheckBox("enableAuthentication");
        enableAuthenticationCheckBox.click();

        try {
            jenkinsConfigurationPanel.applyConfigurationData(configuration);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("'username' must be set", ex.getMessage());
        }

        TextBox usernameTextbox = uiSpecPanel.getTextBox("username");
        usernameTextbox.setText("johndoe");
        try {
            jenkinsConfigurationPanel.applyConfigurationData(configuration);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("'passwordFile' must be set", ex.getMessage());
        }

        TextBox passwordTextField = uiSpecPanel.getTextBox("passwordFile");
        passwordTextField.setText("D:/password.txt");
        jenkinsConfigurationPanel.applyConfigurationData(configuration);
        assertEquals(SecurityMode.BASIC, configuration.getSecurityMode());
        assertEquals("johndoe", configuration.getUsername());
        assertEquals("D:/password.txt", configuration.getPasswordFile());


        enableAuthenticationCheckBox.click();
        jenkinsConfigurationPanel.applyConfigurationData(configuration);
        assertEquals(SecurityMode.NONE, configuration.getSecurityMode());
        assertEquals("", configuration.getUsername());
        assertEquals("", configuration.getPasswordFile());

    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        jenkinsConfigurationPanel = new JenkinsConfigurationPanel(jenkinsRequestManager, false);

        configuration = new JenkinsConfiguration();
        jenkinsConfigurationPanel.loadConfigurationData(configuration);

        uiSpecPanel = new Panel(jenkinsConfigurationPanel.getRootPanel());
    }
}
