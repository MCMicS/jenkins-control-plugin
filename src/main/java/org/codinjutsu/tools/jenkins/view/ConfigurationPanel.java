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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.NumberDocument;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.security.AuthenticationException;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidator;
import org.codinjutsu.tools.jenkins.view.annotation.GuiField;
import org.codinjutsu.tools.jenkins.view.validator.NotNullValidator;
import org.codinjutsu.tools.jenkins.view.validator.UIValidator;
import org.codinjutsu.tools.jenkins.view.validator.UrlValidator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.URL;

@SuppressWarnings({"unchecked"})
public class ConfigurationPanel {

    private static final Color CONNECTION_TEST_SUCCESSFUL_COLOR = new Color(0, 165, 0);
    private static final Color CONNECTION_TEST_FAILED_COLOR = new Color(165, 0, 0);

    @GuiField(validators = URL)
    private JTextField serverUrl;

    private JTextField crumbDataField;

    private JTextField username;
    private JPasswordField passwordField;

    private JTextField buildDelay;
    private JTextField jobRefreshPeriod;
    private JTextField rssRefreshPeriod;

    private JPanel rootPanel;

    private JButton testConnexionButton;
    private JLabel connectionStatusLabel;
    private JPanel debugPanel;
    private JTextPane debugTextPane;

    private JPanel rssStatusFilterPanel;

    private JCheckBox successOrStableCheckBox;
    private JCheckBox unstableOrFailCheckBox;
    private JCheckBox abortedCheckBox;
    private JPanel UploadPatchSettingsPanel;
    private JTextField ReplaceWithSuffix;

    private final FormValidator formValidator;

    private boolean myPasswordModified = false;

    public ConfigurationPanel(final Project project) {

        serverUrl.setName("serverUrl");
        buildDelay.setName("buildDelay");
        jobRefreshPeriod.setName("jobRefreshPeriod");
        rssRefreshPeriod.setName("rssRefreshPeriod");
        username.setName("_username_");

        passwordField.setName("passwordFile");
        crumbDataField.setName("crumbDataFile");

        testConnexionButton.setName("testConnexionButton");
        connectionStatusLabel.setName("connectionStatusLabel");

        successOrStableCheckBox.setName("successOrStableCheckBox");
        unstableOrFailCheckBox.setName("unstableOrFailCheckBox");
        abortedCheckBox.setName("abortedCheckBox");

        rssStatusFilterPanel.setBorder(IdeBorderFactory.createTitledBorder("Event Log Settings", true));

        debugPanel.setVisible(false);

        initDebugTextPane();

        buildDelay.setDocument(new NumberDocument());
        jobRefreshPeriod.setDocument(new NumberDocument());
        rssRefreshPeriod.setDocument(new NumberDocument());

        UploadPatchSettingsPanel.setBorder(IdeBorderFactory.createTitledBorder("Upload a Patch Settings", true));

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

                    RequestManager.getInstance(project).authenticate(serverUrl.getText(), username.getText(), password, crumbDataField.getText());
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
                || !(jenkinsSettings.getCrumbData().equals(crumbDataField.getText()))
                || credentialModified
                || statusToIgnoreModified || (!jenkinsAppSettings.getSuffix().equals(ReplaceWithSuffix.getText()));
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
        jenkinsSettings.setCrumbData(crumbDataField.getText());

        jenkinsAppSettings.setIgnoreSuccessOrStable(successOrStableCheckBox.isSelected());
        jenkinsAppSettings.setDisplayUnstableOrFail(unstableOrFailCheckBox.isSelected());
        jenkinsAppSettings.setDisplayAborted(abortedCheckBox.isSelected());
        jenkinsAppSettings.setSuffix(ReplaceWithSuffix.getText());


        if (StringUtils.isNotBlank(username.getText())) {
            jenkinsSettings.setUsername(username.getText());
        } else {
            jenkinsSettings.setUsername("");
        }

        if (isPasswordModified()) {
            jenkinsSettings.setPassword(getPassword());
            resetPasswordModification();
        }
    }

    public void loadConfigurationData(JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings) {
        serverUrl.setText(jenkinsAppSettings.getServerUrl());
        buildDelay.setText(String.valueOf(jenkinsAppSettings.getBuildDelay()));

        jobRefreshPeriod.setText(String.valueOf(jenkinsAppSettings.getJobRefreshPeriod()));

        rssRefreshPeriod.setText(String.valueOf(jenkinsAppSettings.getRssRefreshPeriod()));

        username.setText(jenkinsSettings.getUsername());
        if (StringUtils.isNotBlank(jenkinsSettings.getUsername())) {
            passwordField.setText(jenkinsSettings.getPassword());
            resetPasswordModification();
        }

        crumbDataField.setText(jenkinsSettings.getCrumbData());

        successOrStableCheckBox.setSelected(jenkinsAppSettings.shouldDisplaySuccessOrStable());
        unstableOrFailCheckBox.setSelected(jenkinsAppSettings.shouldDisplayFailOrUnstable());
        abortedCheckBox.setSelected(jenkinsAppSettings.shouldDisplayAborted());

        ReplaceWithSuffix.setText(String.valueOf(jenkinsAppSettings.getSuffix()));
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
        return ReplaceWithSuffix.getText();
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
