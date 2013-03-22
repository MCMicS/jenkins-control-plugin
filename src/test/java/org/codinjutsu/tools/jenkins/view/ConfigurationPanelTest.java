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

package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.security.AuthenticationException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uispec4j.*;

import static org.codinjutsu.tools.jenkins.JenkinsAppSettings.DEFAULT_BUILD_DELAY;
import static org.codinjutsu.tools.jenkins.JenkinsAppSettings.DUMMY_JENKINS_SERVER_URL;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;

public class ConfigurationPanelTest extends UISpecTestCase {

    private ConfigurationPanel jenkinsConfigurationPanel;
    private JenkinsAppSettings jenkinsAppSettings;
    private JenkinsSettings jenkinsSettings;
    private Panel uiSpecPanel;

    @Mock
    private RequestManager requestManager;

    public void testDisplayWithDefaultValues() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.textEquals(DUMMY_JENKINS_SERVER_URL).check();

        TextBox buildDelayBox = uiSpecPanel.getTextBox("buildDelay");
        buildDelayBox.textEquals(Integer.toString(DEFAULT_BUILD_DELAY)).check();

        TextBox jobRefreshPeriodBox = uiSpecPanel.getTextBox("jobRefreshPeriod");
        jobRefreshPeriodBox.textEquals("0").check();

        TextBox rssRefreshPeriodBox = uiSpecPanel.getTextBox("rssRefreshPeriod");
        rssRefreshPeriodBox.textEquals("0").check();

        TextBox usernameTextbox = uiSpecPanel.getTextBox("_username_");
        usernameTextbox.textIsEmpty().check();

        PasswordField passwordTextField = uiSpecPanel.getPasswordField("passwordFile");
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
        jobRefreshPeriodBox.setText("2");

        TextBox rssRefreshPeriodBox = uiSpecPanel.getTextBox("rssRefreshPeriod");

        rssRefreshPeriodBox.setText("5");


        TextBox usernameTextbox = uiSpecPanel.getTextBox("_username_");
        usernameTextbox.setText("johndoe");

        PasswordField passwordFileField = uiSpecPanel.getPasswordField("passwordFile");
        passwordFileField.setPassword("newPassword");

        TextBox crumbDataField = uiSpecPanel.getTextBox("crumbData");
        crumbDataField.setText("crumbDataValue");

        jenkinsConfigurationPanel.applyConfigurationData(jenkinsAppSettings, jenkinsSettings);

        assertEquals("http://anotherjenkinsserver:1010/jenkins", jenkinsAppSettings.getServerUrl());
        assertEquals(10, jenkinsAppSettings.getBuildDelay());
        assertEquals(2, jenkinsAppSettings.getJobRefreshPeriod());
        assertEquals(5, jenkinsAppSettings.getRssRefreshPeriod());
        assertEquals("johndoe", jenkinsSettings.getUsername());
        assertEquals("newPassword", jenkinsSettings.getPassword());
        assertEquals("crumbDataValue", jenkinsSettings.getCrumbData());
    }


    public void testApplyConfigWithMalformedUrlShouldFail() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("portnawak");

        try {
            jenkinsConfigurationPanel.applyConfigurationData(jenkinsAppSettings, jenkinsSettings);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("URL 'portnawak' is malformed", ex.getMessage());
        }
    }

    public void testApplyConfigWithUrlWithCredentialsShouldFail() throws Exception {

        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("http://david:david@localhost:80");

        try {
            jenkinsConfigurationPanel.applyConfigurationData(jenkinsAppSettings, jenkinsSettings);
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

    public void disabled_testConnectionWithAuthenticationExceptionThrownShouldFail() throws Exception {
        doThrow(new AuthenticationException("ouch")).when(requestManager).authenticate(anyString(), anyString(), anyString(), anyString());


        TextBox serverUrlBox = uiSpecPanel.getTextBox("serverUrl");
        serverUrlBox.setText("http:///bisous");

        Button connexionButton = uiSpecPanel.getButton("testConnexionButton");

        connexionButton.click();

        TextBox connectionStatusLabel = uiSpecPanel.getTextBox("connectionStatusLabel");
        connectionStatusLabel.textEquals("[Fail] ouch").check();
    }

    public void testApplyAuthenticationWithInvalidUserParameters() throws Exception {
        TextBox usernameTextbox = uiSpecPanel.getTextBox("_username_");
        usernameTextbox.setText("johndoe");
        try {
            jenkinsConfigurationPanel.applyConfigurationData(jenkinsAppSettings, jenkinsSettings);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("'passwordFile' must be set", ex.getMessage());
        }

        PasswordField passwordTextField = uiSpecPanel.getPasswordField("passwordFile");
        passwordTextField.setPassword("password");

        jenkinsConfigurationPanel.applyConfigurationData(jenkinsAppSettings, jenkinsSettings);
        assertEquals("johndoe", jenkinsSettings.getUsername());
        assertEquals("password", jenkinsSettings.getPassword());


        usernameTextbox.setText("");
        passwordTextField.setPassword("");
        jenkinsConfigurationPanel.applyConfigurationData(jenkinsAppSettings, jenkinsSettings);
        assertEquals("", jenkinsSettings.getUsername());
        assertEquals("", jenkinsSettings.getPassword());

    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        jenkinsAppSettings = new JenkinsAppSettings();
        jenkinsSettings = new JenkinsSettings() {//TODO Crappy

            private String password;

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public void setPassword(String password) {
                this.password = password;
            }

        };

        jenkinsConfigurationPanel = new ConfigurationPanel(null);

        jenkinsConfigurationPanel.loadConfigurationData(jenkinsAppSettings, jenkinsSettings);

        uiSpecPanel = new Panel(jenkinsConfigurationPanel.getRootPanel());
    }
}
