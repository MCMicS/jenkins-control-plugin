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
import org.codinjutsu.tools.jenkins.view.security.BasicCredentialPanel;
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

    private JPanel rootPanel;

    private JButton testConnexionButton;

    private JLabel connectionStatusLabel;

    private JRadioButton noneRadioButton;
    private JRadioButton basicRadioButton;


    private JLabel securityDescriptionLabel;

    private JButton discoverButton;

    private JPanel cardPanel;
    private final CardLayout cardLayout;


    private final FormValidator formValidator;

    private SecurityMode securityMode = SecurityMode.NONE;

    private final JenkinsRequestManager jenkinsRequestManager;

    private final BasicCredentialPanel credentialPanel = new BasicCredentialPanel();


    public JenkinsConfigurationPanel(final JenkinsRequestManager jenkinsRequestManager) {
        this.jenkinsRequestManager = jenkinsRequestManager;

        serverUrl.setName("serverUrl");
        buildDelay.setName("buildDelay");
        enableJobAutoRefresh.setName("enableJobAutoRefresh");
        jobRefreshPeriod.setName("jobRefreshPeriod");
        enableRssAutoRefresh.setName("enableRssAutoRefresh");
        rssRefreshPeriod.setName("rssRefreshPeriod");
        preferredView.setName("preferredView");
        noneRadioButton.setName("noneRadioButton");
        basicRadioButton.setName("basicRadioButton");

        discoverButton.setToolTipText("Discover the Security Configuration of your Jenkins Server");
        discoverButton.setIcon(GuiUtil.loadIcon("wand.png"));

        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        cardPanel.add(SecurityMode.NONE.name(), new JPanel());
        cardPanel.add(SecurityMode.BASIC.name(), credentialPanel);

        initListeners();

        formValidator = FormValidator.init(this).addValidator(basicRadioButton, new UIValidator<JRadioButton>() {
            public void validate(JRadioButton component) throws ConfigurationException {
                if (basicRadioButton.isSelected()) {
                    credentialPanel.validateInputs();
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
                || !(configuration.getUsername().equals(credentialPanel.getUsernameValue()))
                || !(configuration.getPassword().equals(credentialPanel.getPasswordValue()))
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

        configuration.setUsername(credentialPanel.getUsernameValue());
        configuration.setPassword(credentialPanel.getPasswordValue());
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

        setSecurityMode(configuration.getSecurityMode(), configuration.getUsername(), configuration.getPassword());

    }


    private void setSecurityMode(SecurityMode securityMode, @Nullable String username, @Nullable String passwordFile) {
        cardLayout.show(cardPanel, securityMode.name());
        if (SecurityMode.BASIC.equals(securityMode)) {
            credentialPanel.updateFields(username, passwordFile);
            basicRadioButton.setSelected(true);
        } else {
            credentialPanel.resetFields();
        }
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
                        serverUrl.getText(), securityMode, credentialPanel.getUsernameValue(), credentialPanel.getPasswordValue());


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


        ButtonGroup securityModeGroup = new ButtonGroup();
        securityModeGroup.add(noneRadioButton);
        securityModeGroup.add(basicRadioButton);

        noneRadioButton.addMouseListener(new SecurityDescriptionListener("Jenkins security is disabled"));
        basicRadioButton.addMouseListener(new SecurityDescriptionListener("Jenkins security is enabled"));

        ActionListener securityModeListener = new SecurityModeListener();
        noneRadioButton.addActionListener(securityModeListener);
        noneRadioButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                final boolean isSelected = noneRadioButton.isSelected();
                if (isSelected) {
                    credentialPanel.resetFields();
                }
            }
        });

        noneRadioButton.setActionCommand(SecurityMode.NONE.name());

        basicRadioButton.addActionListener(securityModeListener);
        basicRadioButton.setActionCommand(SecurityMode.BASIC.name());
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

    private class SecurityModeListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            String actionCommand = event.getActionCommand();
            cardLayout.show(cardPanel, actionCommand);
            securityMode = SecurityMode.valueOf(actionCommand);
        }
    }

    private class SecurityDescriptionListener extends MouseAdapter {

        private final String description;

        private SecurityDescriptionListener(String description) {
            this.description = description;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            securityDescriptionLabel.setText(description);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            securityDescriptionLabel.setText("");
        }
    }
}
