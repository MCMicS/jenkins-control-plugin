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

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.AuthenticationResult;
import org.codinjutsu.tools.jenkins.logic.JenkinsRequestManager;
import org.codinjutsu.tools.jenkins.security.AuthenticationException;
import org.codinjutsu.tools.jenkins.security.SecurityMode;
import org.codinjutsu.tools.jenkins.security.SecurityResolver;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.SwingUtils;
import org.codinjutsu.tools.jenkins.view.action.ThreadFunctor;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidator;
import org.codinjutsu.tools.jenkins.view.annotation.GuiField;
import org.codinjutsu.tools.jenkins.view.validator.NotNullValidator;
import org.codinjutsu.tools.jenkins.view.validator.UIValidator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.*;

@SuppressWarnings({"unchecked"})
public class JenkinsConfigurationPanel {

    private static final Color CONNECTION_TEST_SUCCESSFUL_COLOR = new Color(0, 165, 0);
    private static final Color CONNECTION_TEST_FAILED_COLOR = new Color(165, 0, 0);


    @GuiField(validators = {NOTNULL, URL})
    private JTextField serverUrl;

    @GuiField(validators = {NOTNULL, POSITIVE_INTEGER})
    private JTextField buildDelay;

    private JCheckBox enableJobAutoRefresh;

    @GuiField(validators = {NOTNULL, STRICT_POSITIVE_INTEGER})
    private JTextField jobRefreshPeriod;

    private JCheckBox enableRssAutoRefresh;

    @GuiField(validators = {NOTNULL, STRICT_POSITIVE_INTEGER})
    private JTextField rssRefreshPeriod;

    private JTextField preferredView;

    private JCheckBox enableAuthentication;
    private JTextField username;
    private LabeledComponent<TextFieldWithBrowseButton> passwordFile;

    private JPanel rootPanel;

    private JButton testConnexionButton;

    private JLabel connectionStatusLabel;

    private final FormValidator formValidator;

    private JButton discoverButton;


    private SecurityMode securityMode = SecurityMode.NONE;

    private final JenkinsRequestManager jenkinsRequestManager;

    public JenkinsConfigurationPanel(JenkinsRequestManager jenkinsRequestManager) {
        this(jenkinsRequestManager, true);
    }

    public JenkinsConfigurationPanel(final JenkinsRequestManager jenkinsRequestManager, boolean installBrowserFileBrowser) {
        this.jenkinsRequestManager = jenkinsRequestManager;

        serverUrl.setName("serverUrl");
        buildDelay.setName("buildDelay");
        enableJobAutoRefresh.setName("enableJobAutoRefresh");
        jobRefreshPeriod.setName("jobRefreshPeriod");
        enableRssAutoRefresh.setName("enableRssAutoRefresh");
        rssRefreshPeriod.setName("rssRefreshPeriod");
        preferredView.setName("preferredView");
        enableAuthentication.setName("enableAuthentication");
        username.setName("username");

        passwordFile.getComponent().getTextField().setName("passwordFile");

        discoverButton.setToolTipText("Discover the Security Configuration of your Jenkins Server");
        discoverButton.setIcon(GuiUtil.loadIcon("wand.png"));

        initListeners();

        if (installBrowserFileBrowser) {
            addBrowserLinkToPasswordFile();
        }

        formValidator = FormValidator.init(this)
                .addValidator(enableAuthentication, new UIValidator<JCheckBox>() {
                    public void validate(JCheckBox component) throws ConfigurationException {
                        if (enableAuthentication.isSelected()) {
                            new NotNullValidator().validate(username);
                            if (passwordFile.isEnabled()) {    //TODO a revoir
                                String value = passwordFile.getComponent().getText();
                                if (value == null || "".equals(value)) {
                                    throw new ConfigurationException("'" + passwordFile.getComponent().getTextField().getName() + "' must be set");
                                }
                            }
                        }
                    }
                });


    }


    public boolean isModified(JenkinsConfiguration configuration) {
        return !configuration.getServerUrl().equals(serverUrl.getText())
                || !(configuration.getBuildDelay() == Integer.parseInt(buildDelay.getText()))
                || !(configuration.isEnableJobAutoRefresh() == enableJobAutoRefresh.isSelected())
                || !(configuration.getJobRefreshPeriod() == Integer.parseInt(jobRefreshPeriod.getText()))
                || !(configuration.isEnableRssAutoRefresh() == enableRssAutoRefresh.isSelected())
                || !(configuration.getRssRefreshPeriod() == Integer.parseInt(rssRefreshPeriod.getText()))
                || !(configuration.getPreferredView().equals(preferredView.getText()))
                || !(configuration.getSecurityMode() == securityMode)
                || !(configuration.getUsername().equals(username.getText()))
                || !(configuration.getPasswordFile().equals(passwordFile.getComponent().getText()))
                ;
    }


    public void applyConfigurationData(JenkinsConfiguration configuration) throws ConfigurationException {
        formValidator.validate();

        configuration.setServerUrl(serverUrl.getText());
        configuration.setDelay(Integer.valueOf(buildDelay.getText()));
        configuration.setEnableJobAutoRefresh(enableJobAutoRefresh.isSelected());
        configuration.setJobRefreshPeriod(Integer.valueOf(jobRefreshPeriod.getText()));
        configuration.setEnableRssAutoRefresh(enableRssAutoRefresh.isSelected());
        configuration.setRssRefreshPeriod(Integer.valueOf(rssRefreshPeriod.getText()));
        configuration.setRssRefreshPeriod(Integer.valueOf(rssRefreshPeriod.getText()));
        configuration.setPreferredView(preferredView.getText());
        configuration.setSecurityMode(securityMode);

        configuration.setUsername(username.getText());
        configuration.setPasswordFile(passwordFile.getComponent().getText());
    }

    public void loadConfigurationData(JenkinsConfiguration configuration) {
        serverUrl.setText(configuration.getServerUrl());
        buildDelay.setText(String.valueOf(configuration.getBuildDelay()));

        boolean jobAutoRefresh = configuration.isEnableJobAutoRefresh();
        enableJobAutoRefresh.setSelected(jobAutoRefresh);
        jobRefreshPeriod.setText(String.valueOf(configuration.getJobRefreshPeriod()));
        jobRefreshPeriod.setEnabled(jobAutoRefresh);

        boolean rssAutoRefresh = configuration.isEnableRssAutoRefresh();
        enableRssAutoRefresh.setSelected(rssAutoRefresh);
        rssRefreshPeriod.setText(String.valueOf(configuration.getRssRefreshPeriod()));
        rssRefreshPeriod.setEnabled(rssAutoRefresh);

        preferredView.setText(configuration.getPreferredView());

        setSecurityMode(configuration.getSecurityMode(), configuration.getUsername(), configuration.getPasswordFile());
    }


    private void setSecurityMode(SecurityMode securityMode, @Nullable String usernameValue, @Nullable String passwordFileValue) {
        boolean isEnableAuthentication = SecurityMode.BASIC.equals(securityMode);

        enableAuthentication.setSelected(isEnableAuthentication);

        username.setText(usernameValue);
        username.setEnabled(isEnableAuthentication);

        passwordFile.getComponent().setText(passwordFileValue);
        passwordFile.setEnabled(isEnableAuthentication);


        this.securityMode = securityMode;
    }


    public JPanel getRootPanel() {
        return rootPanel;
    }


    private void initListeners() {
        String resetPeriodValue = Integer.toString(JenkinsConfiguration.RESET_PERIOD_VALUE);

        testConnexionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final AuthenticationResult authResult = jenkinsRequestManager.authenticate(
                        serverUrl.getText(), securityMode, username.getText(), passwordFile.getComponent().getText());


                SwingUtils.runInSwingThread(new ThreadFunctor() {
                    public void run() {
                        Color foregroundColor = CONNECTION_TEST_FAILED_COLOR;
                        if (AuthenticationResult.SUCCESSFULL.equals(authResult)) {
                            foregroundColor = CONNECTION_TEST_SUCCESSFUL_COLOR;
                        }

                        connectionStatusLabel.setForeground(foregroundColor);
                        connectionStatusLabel.setText(authResult.getLabel());
                    }
                });

            }
        });

        discoverButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (StringUtils.isNotEmpty(serverUrl.getText())) {
                    try {
                        SecurityMode SecurityMode = SecurityResolver.resolve(serverUrl.getText());
                        setSecurityMode(SecurityMode, null, null);
                        connectionStatusLabel.setText("");
                    } catch (final AuthenticationException authEx) {
                        SwingUtils.runInSwingThread(new ThreadFunctor() {
                            public void run() {
                                connectionStatusLabel.setForeground(CONNECTION_TEST_FAILED_COLOR);
                                connectionStatusLabel.setText(authEx.getMessage());
                            }
                        });
                    }
                }
            }
        });

        enableJobAutoRefresh.addItemListener(new EnablerFieldListener(enableJobAutoRefresh,
                jobRefreshPeriod, resetPeriodValue));
        enableRssAutoRefresh.addItemListener(new EnablerFieldListener(enableRssAutoRefresh,
                rssRefreshPeriod, resetPeriodValue));

        enableAuthentication.addItemListener(new EnablerFieldListener(enableAuthentication,
                username, JenkinsConfiguration.RESET_STR_VALUE));
        enableAuthentication.addItemListener(new EnablerPasswordFileListener(enableAuthentication,
                passwordFile, JenkinsConfiguration.RESET_STR_VALUE));

        enableAuthentication.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (enableAuthentication.isSelected()) {
                    securityMode = SecurityMode.BASIC;
                } else {
                    securityMode = SecurityMode.NONE;
                }
            }
        });
    }

    void addBrowserLinkToPasswordFile() {
        passwordFile.getComponent().addBrowseFolderListener("Jenkins User password File", "", null,
                new FileChooserDescriptor(true, false, false, false, false, false));
    }

    private class EnablerFieldListener implements ItemListener {
        private final JCheckBox enablerCheckBox;
        private final JTextField fieldToEnable;
        private final String resetValue;


        private EnablerFieldListener(JCheckBox enablerCheckBox,
                                     JTextField fieldToEnable, String resetValue) {
            this.enablerCheckBox = enablerCheckBox;
            this.fieldToEnable = fieldToEnable;
            this.resetValue = resetValue;
        }


        public void itemStateChanged(ItemEvent event) {
            final boolean isSelected = enablerCheckBox.isSelected();
            if (!isSelected) {
                fieldToEnable.setText(resetValue);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fieldToEnable.setEnabled(isSelected);
                }
            });
        }
    }

   private class EnablerPasswordFileListener implements ItemListener {
        private final JCheckBox enablerCheckBox;
        private final LabeledComponent<TextFieldWithBrowseButton> fieldToEnable;
        private final String resetValue;


        private EnablerPasswordFileListener(JCheckBox enablerCheckBox,
                                            LabeledComponent<TextFieldWithBrowseButton> fieldToEnable, String resetValue) {
            this.enablerCheckBox = enablerCheckBox;
            this.fieldToEnable = fieldToEnable;
            this.resetValue = resetValue;
        }


        public void itemStateChanged(ItemEvent event) {
            final boolean isSelected = enablerCheckBox.isSelected();
            if (!isSelected) {
                fieldToEnable.getComponent().setText(resetValue);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fieldToEnable.setEnabled(isSelected);
                }
            });
        }
    }
}
