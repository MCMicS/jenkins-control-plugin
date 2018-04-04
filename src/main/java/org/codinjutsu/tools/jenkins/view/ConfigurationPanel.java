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

import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.URL;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.security.AuthenticationException;
import org.codinjutsu.tools.jenkins.security.JenkinsVersion;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidator;
import org.codinjutsu.tools.jenkins.view.annotation.GuiField;
import org.codinjutsu.tools.jenkins.view.validator.NotNullValidator;
import org.codinjutsu.tools.jenkins.view.validator.UIValidator;
import org.codinjutsu.tools.jenkins.view.validator.UrlValidator;

import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.NumberDocument;

@SuppressWarnings({"unchecked"})
public class ConfigurationPanel {

    private static final Color CONNECTION_TEST_SUCCESSFUL_COLOR = JBColor.GREEN;
    private static final Color CONNECTION_TEST_FAILED_COLOR = JBColor.RED;

    @GuiField(validators = URL)
    private JTextField serverUrl;

    private JTextField crumbDataField;

    private JTextField username;
    private JPasswordField passwordField;

    private JTextField buildDelay;
    private JTextField jobRefreshPeriod;
    private JTextField rssRefreshPeriod;
    private JTextField numBuildRetries;

    private JPanel rootPanel;

    private JButton testConnexionButton;
    private JLabel connectionStatusLabel;
    private JPanel debugPanel;
    private JTextPane debugTextPane;

    private JPanel rssStatusFilterPanel;

    private JCheckBox successOrStableCheckBox;
    private JCheckBox unstableOrFailCheckBox;
    private JCheckBox abortedCheckBox;

    private JPanel uploadPatchSettingsPanel;
    private JTextField replaceWithSuffix;
    private JRadioButton version1RadioButton;
    private JRadioButton version2RadioButton;

    private final FormValidator formValidator;

    private boolean myPasswordModified = false;

    public ConfigurationPanel(final Project project) {

        serverUrl.setName("serverUrl");
        buildDelay.setName("buildDelay");
        jobRefreshPeriod.setName("jobRefreshPeriod");
        rssRefreshPeriod.setName("rssRefreshPeriod");
        numBuildRetries.setName("numBuildRetries");
        username.setName("_username_");

        passwordField.setName("passwordFile");
        crumbDataField.setName("crumbDataFile");

        testConnexionButton.setName("testConnexionButton");
        connectionStatusLabel.setName("connectionStatusLabel");

        successOrStableCheckBox.setName("successOrStableCheckBox");
        unstableOrFailCheckBox.setName("unstableOrFailCheckBox");
        abortedCheckBox.setName("abortedCheckBox");
        version1RadioButton.setName("version1RadioButton");
        version2RadioButton.setName("version2RadioButton");

        rssStatusFilterPanel.setBorder(IdeBorderFactory.createTitledBorder("Event Log Settings", true));

        debugPanel.setVisible(false);

        initDebugTextPane();

        buildDelay.setDocument(new NumberDocument());
        jobRefreshPeriod.setDocument(new NumberDocument());
        rssRefreshPeriod.setDocument(new NumberDocument());
        numBuildRetries.setDocument(new NumberDocument());

        uploadPatchSettingsPanel.setBorder(IdeBorderFactory.createTitledBorder("Upload a Patch Settings", true));

        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                myPasswordModified = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                myPasswordModified = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                myPasswordModified = true;
            }
        });

        testConnexionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    debugPanel.setVisible(false);

                    new NotNullValidator().validate(serverUrl);
                    new UrlValidator().validate(serverUrl);

                    JenkinsSettings jenkinsSettings = JenkinsSettings.getSafeInstance(project);

                    String password = isPasswordModified() ? getPassword() : jenkinsSettings.getPassword();

                    JenkinsVersion version = version1RadioButton.isSelected() ? JenkinsVersion.VERSION_1 : JenkinsVersion.VERSION_2;

                    RequestManager.getInstance(project).authenticate(serverUrl.getText(), username.getText(), password, crumbDataField.getText(), version);
                    setConnectionFeedbackLabel(CONNECTION_TEST_SUCCESSFUL_COLOR, "Successful");
                    setPassword(password);
                } catch (Exception ex) {
                    setConnectionFeedbackLabel(CONNECTION_TEST_FAILED_COLOR, "[Fail] " + ex.getMessage());
                    if (ex instanceof AuthenticationException) {
                        AuthenticationException authenticationException = (AuthenticationException) ex;
                        String responseBody = authenticationException.getResponseBody();
                        if (StringUtils.isNotBlank(responseBody)) {
                            debugPanel.setVisible(true);
                            debugTextPane.setText(responseBody);
                        }
                    }
                }
            }
        });

        formValidator = FormValidator.init(this)
                .addValidator(username, new UIValidator<JTextField>() {
                    public void validate(JTextField component) throws ConfigurationException {
                        if (StringUtils.isNotBlank(component.getText())) {
                            String password = getPassword();
                            if (StringUtils.isBlank(password)) {
                                throw new ConfigurationException(String.format("'%s' must be set", passwordField.getName()));
                            }
                        }
                    }
                });

    }

    //TODO use annotation to create a guiwrapper so isModified could be simplified
    public boolean isModified(JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings) {
        boolean credentialModified = !(jenkinsSettings.getUsername().equals(username.getText()))
                || isPasswordModified();

        boolean statusToIgnoreModified = successOrStableCheckBox.isSelected() != jenkinsAppSettings.shouldDisplaySuccessOrStable()
                || unstableOrFailCheckBox.isSelected() != jenkinsAppSettings.shouldDisplayFailOrUnstable()
                || abortedCheckBox.isSelected() != jenkinsAppSettings.shouldDisplayAborted();

        return !jenkinsAppSettings.getServerUrl().equals(serverUrl.getText())
                || !(jenkinsAppSettings.getBuildDelay() == getBuildDelay())
                || !(jenkinsAppSettings.getJobRefreshPeriod() == getJobRefreshPeriod())
                || !(jenkinsAppSettings.getRssRefreshPeriod() == getRssRefreshPeriod())
                || !(jenkinsAppSettings.getNumBuildRetries() == getNumBuildRetries())
                || !(jenkinsSettings.getCrumbData().equals(crumbDataField.getText()))
                || credentialModified
                || statusToIgnoreModified || (!jenkinsAppSettings.getSuffix().equals(replaceWithSuffix.getText()));
    }

    //TODO use annotation to create a guiwrapper so isModified could be simplified
    public void applyConfigurationData(JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings) throws ConfigurationException {
        formValidator.validate();

        if (!StringUtils.equals(jenkinsAppSettings.getServerUrl(), serverUrl.getText())) {
            jenkinsSettings.getFavoriteJobs().clear();
            jenkinsSettings.setLastSelectedView(null);
        }

        jenkinsAppSettings.setServerUrl(serverUrl.getText());
        jenkinsAppSettings.setDelay(getBuildDelay());
        jenkinsAppSettings.setJobRefreshPeriod(getJobRefreshPeriod());
        jenkinsAppSettings.setRssRefreshPeriod(getRssRefreshPeriod());
        jenkinsAppSettings.setNumBuildRetries(getNumBuildRetries());
        jenkinsSettings.setCrumbData(crumbDataField.getText());

        jenkinsAppSettings.setIgnoreSuccessOrStable(successOrStableCheckBox.isSelected());
        jenkinsAppSettings.setDisplayUnstableOrFail(unstableOrFailCheckBox.isSelected());
        jenkinsAppSettings.setDisplayAborted(abortedCheckBox.isSelected());
        jenkinsAppSettings.setSuffix(replaceWithSuffix.getText());

        if (StringUtils.isNotBlank(username.getText())) {
            jenkinsSettings.setUsername(username.getText());
        } else {
            jenkinsSettings.setUsername("");
        }

        if (isPasswordModified()) {
            jenkinsSettings.setPassword(getPassword());
            resetPasswordModification();
        }
        if (version1RadioButton.isSelected()) {
            jenkinsSettings.setVersion(JenkinsVersion.VERSION_1);
        } else {
            jenkinsSettings.setVersion(JenkinsVersion.VERSION_2);
        }
    }

    public void loadConfigurationData(JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings) {
        serverUrl.setText(jenkinsAppSettings.getServerUrl());
        buildDelay.setText(String.valueOf(jenkinsAppSettings.getBuildDelay()));

        jobRefreshPeriod.setText(String.valueOf(jenkinsAppSettings.getJobRefreshPeriod()));

        rssRefreshPeriod.setText(String.valueOf(jenkinsAppSettings.getRssRefreshPeriod()));
        numBuildRetries.setText(String.valueOf(jenkinsAppSettings.getNumBuildRetries()));

        username.setText(jenkinsSettings.getUsername());
        if (StringUtils.isNotBlank(jenkinsSettings.getUsername())) {
            passwordField.setText(jenkinsSettings.getPassword());
            resetPasswordModification();
        }

        crumbDataField.setText(jenkinsSettings.getCrumbData());

        successOrStableCheckBox.setSelected(jenkinsAppSettings.shouldDisplaySuccessOrStable());
        unstableOrFailCheckBox.setSelected(jenkinsAppSettings.shouldDisplayFailOrUnstable());
        abortedCheckBox.setSelected(jenkinsAppSettings.shouldDisplayAborted());

        replaceWithSuffix.setText(String.valueOf(jenkinsAppSettings.getSuffix()));

        if (jenkinsSettings.getVersion().equals(JenkinsVersion.VERSION_1)) {
            version1RadioButton.setSelected(true);
            version2RadioButton.setSelected(false);
        } else {
            version1RadioButton.setSelected(false);
            version2RadioButton.setSelected(true);
        }
    }

    private boolean isPasswordModified() {
        return myPasswordModified;
    }

    private void initDebugTextPane() {
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        HTMLDocument htmlDocument = new HTMLDocument();

        debugTextPane.setEditable(false);
        debugTextPane.setBackground(Color.WHITE);
        debugTextPane.setEditorKit(htmlEditorKit);
        htmlEditorKit.install(debugTextPane);
        debugTextPane.setDocument(htmlDocument);
    }

    private void resetPasswordModification() {
        myPasswordModified = false;
    }

    private void setPassword(String password) {
        passwordField.setText(StringUtils.isBlank(password) ? null : password);
    }

    private String getPassword() {
        return String.valueOf(passwordField.getPassword());
    }

    private int getRssRefreshPeriod() {
        String period = rssRefreshPeriod.getText();
        if (StringUtils.isNotBlank(period)) {
            return Integer.parseInt(period);
        }
        return 0;
    }

    private int getNumBuildRetries() {
        String period = numBuildRetries.getText();
        if (StringUtils.isNotBlank(period)) {
            return Integer.parseInt(period);
        }
        return 1;
    }

    private int getJobRefreshPeriod() {
        String period = jobRefreshPeriod.getText();
        if (StringUtils.isNotBlank(period)) {
            return Integer.parseInt(period);
        }
        return 0;
    }

    private int getBuildDelay() {
        String delay = buildDelay.getText();
        if (StringUtils.isNotBlank(delay)) {
            return Integer.parseInt(delay);
        }
        return 0;
    }

    private String getSuffix() {
        return replaceWithSuffix.getText();
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    private void setConnectionFeedbackLabel(final Color labelColor, final String labelText) {
        GuiUtil.runInSwingThread(new Runnable() {
            public void run() {
                connectionStatusLabel.setForeground(labelColor);
                connectionStatusLabel.setText(labelText);
            }
        });
    }
}
