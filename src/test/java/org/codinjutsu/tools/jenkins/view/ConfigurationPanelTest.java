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

package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.security.AuthenticationException;
import org.codinjutsu.tools.jenkins.security.SecurityMode;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uispec4j.*;

import static org.codinjutsu.tools.jenkins.JenkinsConfiguration.DEFAULT_BUILD_DELAY;
import static org.codinjutsu.tools.jenkins.JenkinsConfiguration.DUMMY_JENKINS_SERVER_URL;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;

public class ConfigurationPanelTest extends UISpecTestCase {

    private ConfigurationPanel jenkinsConfigurationPanel;
    private JenkinsConfiguration configuration;
    private Panel uiSpecPanel;

    @Mock
    private RequestManager requestManager;

    public void testDisplayWithDefaultValues() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.textEquals(DUMMY_JENKINS_SERVER_URL).check();

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

        PasswordField passwordTextField = uiSpecPanel.getPasswordField("passwordFile");
        assertFalse(passwordTextField.isEnabled());
        passwordTextField.passwordEquals("").check();

        TextBox crumbDataTextField = uiSpecPanel.getTextBox("crumbData");
        crumbDataTextField.textIsEmpty().check();

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

        PasswordField passwordFileField = uiSpecPanel.getPasswordField("passwordFile");
        assertTrue(passwordFileField.isEnabled());
        passwordFileField.setPassword("newPassword");

        TextBox crumbDataField = uiSpecPanel.getTextBox("crumbData");
        assertTrue(crumbDataField.isEnabled());
        crumbDataField.setText("crumbDataValue");

        jenkinsConfigurationPanel.applyConfigurationData(configuration);

        assertEquals("http://anotherjenkinsserver:1010/jenkins", configuration.getServerUrl());
        assertEquals(10, configuration.getBuildDelay());
        assertEquals(2, configuration.getJobRefreshPeriod());
        assertEquals(0, configuration.getRssRefreshPeriod());
        assertTrue(configuration.isEnableJobAutoRefresh());
        assertFalse(configuration.isEnableRssAutoRefresh());
        assertEquals(SecurityMode.BASIC, configuration.getSecurityMode());
        assertEquals("johndoe", configuration.getUsername());
//TODO       assertEquals("password.txt", configuration.getPassword());
        assertEquals("crumbDataValue", configuration.getCrumbFile());
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

    public void testApplyConfigWithUrlWithCredentialsShouldFail() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("http://david:david@localhost:80");

        try {
            jenkinsConfigurationPanel.applyConfigurationData(configuration);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("Credentials should not be embedded in the url. Use the above form instead.", ex.getMessage());
        }
    }

    public void testConnectionWithEmptyServerUrlShouldFail() throws Exception {
        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("");

        Button connexionButton = uiSpecPanel.getButton("testConnexionButton");

        connexionButton.click();

        TextBox connectionStatusLabel = uiSpecPanel.getTextBox("connectionStatusLabel");
        connectionStatusLabel.textEquals("[Fail] 'serverUrl' must be set").check();
    }

    public void testConnectionWithEmptyUrlShouldFail() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("");

        Button connexionButton = uiSpecPanel.getButton("testConnexionButton");

        connexionButton.click();

        TextBox connectionStatusLabel = uiSpecPanel.getTextBox("connectionStatusLabel");
        connectionStatusLabel.textEquals("[Fail] 'serverUrl' must be set").check();
    }

    public void testConnectionWithUrlFilledWithCredentialsShouldFail() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("http://david:david@localhost:80");

        Button connexionButton = uiSpecPanel.getButton("testConnexionButton");

        connexionButton.click();

        TextBox connectionStatusLabel = uiSpecPanel.getTextBox("connectionStatusLabel");
        connectionStatusLabel.textEquals("[Fail] Credentials should not be embedded in the url. Use the above form instead.").check();
    }

    public void testConnectionWithAuthenticationExceptionThrownShouldFail() throws Exception {
        doThrow(new AuthenticationException("ouch")).when(requestManager).authenticate(anyString(), any(SecurityMode.class), anyString(), anyString(), anyString());


        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("http:///bisous");

        Button connexionButton = uiSpecPanel.getButton("testConnexionButton");

        connexionButton.click();

        TextBox connectionStatusLabel = uiSpecPanel.getTextBox("connectionStatusLabel");
        connectionStatusLabel.textEquals("[Fail] ouch").check();
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

        PasswordField passwordTextField = uiSpecPanel.getPasswordField("passwordFile");
        passwordTextField.setPassword("password");

        jenkinsConfigurationPanel.applyConfigurationData(configuration);
        assertEquals(SecurityMode.BASIC, configuration.getSecurityMode());
        assertEquals("johndoe", configuration.getUsername());
        assertEquals("password", configuration.getPassword());


        enableAuthenticationCheckBox.click();
        jenkinsConfigurationPanel.applyConfigurationData(configuration);
        assertEquals(SecurityMode.NONE, configuration.getSecurityMode());
        assertEquals("", configuration.getUsername());
        assertEquals("", configuration.getPassword());

    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        jenkinsConfigurationPanel = new ConfigurationPanel(requestManager);

        configuration = new JenkinsConfiguration() { //should spy this object
            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public void setPassword(String password) {
                this.password = password;
            }
        };
        jenkinsConfigurationPanel.loadConfigurationData(configuration);

        uiSpecPanel = new Panel(jenkinsConfigurationPanel.getRootPanel());
    }
}
