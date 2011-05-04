package org.codinjustu.tools.jenkins.view;

import org.codinjustu.tools.jenkins.JenkinsConfiguration;
import org.codinjustu.tools.jenkins.exception.ConfigurationException;
import org.uispec4j.CheckBox;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.UISpecTestCase;

import static org.codinjustu.tools.jenkins.JenkinsConfiguration.DEFAULT_BUILD_DELAY;
import static org.codinjustu.tools.jenkins.JenkinsConfiguration.DEFAULT_JENKINS_SERVER_URL;

public class JenkinsConfigurationPanelTest extends UISpecTestCase {

    private JenkinsConfigurationPanel jenkinsConfigurationPanel;
    private JenkinsConfiguration configuration;


    public void test_displayWithDefaultValues() throws Exception {
        Panel panel = new Panel(jenkinsConfigurationPanel.getRootPanel());

        TextBox serverUrlBox = panel.getTextBox("serverUrl");
        serverUrlBox.textEquals(DEFAULT_JENKINS_SERVER_URL).check();

        TextBox buildDelayBox = panel.getTextBox("buildDelay");
        buildDelayBox.textEquals(Integer.toString(DEFAULT_BUILD_DELAY)).check();

        CheckBox enableJobCheckBox = panel.getCheckBox("enableJobAutoRefresh");
        assertFalse(enableJobCheckBox.isSelected());

        TextBox jobRefreshPeriodBox = panel.getTextBox("jobRefreshPeriod");
        jobRefreshPeriodBox.textEquals("0").check();
        assertFalse(jobRefreshPeriodBox.isEnabled());

        CheckBox enableRssCheckBox = panel.getCheckBox("enableRssAutoRefresh");
        assertFalse(enableRssCheckBox.isSelected());

        TextBox rssRefreshPeriodBox = panel.getTextBox("rssRefreshPeriod");
        rssRefreshPeriodBox.textEquals("0").check();
        assertFalse(rssRefreshPeriodBox.isEnabled());
    }


    public void test_validationOk() throws Exception {
        Panel panel = new Panel(jenkinsConfigurationPanel.getRootPanel());

        TextBox serverUrlBox = panel.getTextBox("serverUrl");
        serverUrlBox.setText("http://anotherjenkinsserver:1010/jenkins");

        TextBox buildDelay = panel.getTextBox("buildDelay");
        buildDelay.setText("10");

        TextBox jobRefreshPeriodBox = panel.getTextBox("jobRefreshPeriod");
        CheckBox enableJobCheckBox = panel.getCheckBox("enableJobAutoRefresh");

        assertFalse(jobRefreshPeriodBox.isEnabled());
        enableJobCheckBox.click();
        assertTrue(jobRefreshPeriodBox.isEnabled());
        jobRefreshPeriodBox.setText("2");

        TextBox rssRefreshPeriodBox = panel.getTextBox("rssRefreshPeriod");
        CheckBox enableRssCheckBox = panel.getCheckBox("enableRssAutoRefresh");

        assertFalse(rssRefreshPeriodBox.isEnabled());
        enableRssCheckBox.click();
        assertTrue(rssRefreshPeriodBox.isEnabled());
        rssRefreshPeriodBox.setText("5");
        enableRssCheckBox.click();
        rssRefreshPeriodBox.textEquals("0").check();
        assertFalse(rssRefreshPeriodBox.isEnabled());

        jenkinsConfigurationPanel.applyConfigurationData(configuration);

        assertEquals("http://anotherjenkinsserver:1010/jenkins", configuration.getServerUrl());
        assertEquals(10, configuration.getBuildDelay());
        assertEquals(2, configuration.getJobRefreshPeriod());
        assertEquals(0, configuration.getRssRefreshPeriod());
        assertTrue(configuration.isEnableJobAutoRefresh());
        assertFalse(configuration.isEnableRssAutoRefresh());
    }


    public void test_applyConfigWithEmptyParamValueShouldFail() throws Exception {
        Panel panel = new Panel(jenkinsConfigurationPanel.getRootPanel());

        TextBox serverUrlBox = panel.getTextBox("serverUrl");
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


    public void test_applyConfigWithMalformedUrlShouldFail() throws Exception {
        Panel panel = new Panel(jenkinsConfigurationPanel.getRootPanel());

        TextBox serverUrlBox = panel.getTextBox("serverUrl");
        serverUrlBox.setText("portnawak");

        try {
            jenkinsConfigurationPanel.applyConfigurationData(configuration);
            fail();
        } catch (ConfigurationException ex) {
            assertEquals("URL 'portnawak' is malformed", ex.getMessage());
        }
    }


    public void test_applyConfigWithInvalidIntegerShouldFail() throws Exception {
        Panel panel = new Panel(jenkinsConfigurationPanel.getRootPanel());

        CheckBox enableJobCheckBox = panel.getCheckBox("enableJobAutoRefresh");
        enableJobCheckBox.click();

        TextBox jobRefreshPeriod = panel.getTextBox("jobRefreshPeriod");
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


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jenkinsConfigurationPanel = new JenkinsConfigurationPanel();
        configuration = new JenkinsConfiguration();
        jenkinsConfigurationPanel.loadConfigurationData(configuration);
    }
}
