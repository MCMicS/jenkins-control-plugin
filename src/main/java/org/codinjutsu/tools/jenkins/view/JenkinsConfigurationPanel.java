package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.AuthenticationResult;
import org.codinjutsu.tools.jenkins.logic.DefaultJenkinsRequestManager;
import org.codinjutsu.tools.jenkins.logic.JenkinsRequestManager;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidator;
import org.codinjutsu.tools.jenkins.view.annotation.GuiField;
import org.codinjutsu.tools.jenkins.view.validator.NotNullValidator;
import org.codinjutsu.tools.jenkins.view.validator.UIValidator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.*;

public class JenkinsConfigurationPanel {

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
    private JPasswordField password;

    private JPanel rootPanel;

    private JButton testConnexionButton;

    private JLabel connectionStatusLabel;

    private FormValidator formValidator;


    public JenkinsConfigurationPanel(final JenkinsRequestManager jenkinsRequestManager) {
        serverUrl.setName("serverUrl");
        buildDelay.setName("buildDelay");
        enableJobAutoRefresh.setName("enableJobAutoRefresh");
        jobRefreshPeriod.setName("jobRefreshPeriod");
        enableRssAutoRefresh.setName("enableRssAutoRefresh");
        rssRefreshPeriod.setName("rssRefreshPeriod");
        preferredView.setName("preferredView");
        enableAuthentication.setName("enableAuthentication");
        username.setName("username");
        password.setName("password");

        initListeners();

        formValidator = FormValidator.init(this)
                .addValidator(enableAuthentication, new UIValidator<JCheckBox>() {
                    public void validate(JCheckBox component) throws ConfigurationException {
                        if (enableAuthentication.isSelected()) {
                            new NotNullValidator().validate(username);
                            new NotNullValidator().validate(password);
                        }
                    }
                });

        testConnexionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AuthenticationResult authResult = jenkinsRequestManager.testConnexion(serverUrl.getText(), enableAuthentication.isSelected(), username.getText(), String.valueOf(password.getPassword()));
                connectionStatusLabel.setText(authResult.getLabel());
            }
        });
    }


    public boolean isModified(JenkinsConfiguration configuration) {
        return  !configuration.getServerUrl().equals(serverUrl.getText())
                || !(configuration.getBuildDelay() == Integer.parseInt(buildDelay.getText()))
                || !(configuration.isEnableJobAutoRefresh() == enableJobAutoRefresh.isSelected())
                || !(configuration.getJobRefreshPeriod() == Integer.parseInt(jobRefreshPeriod.getText()))
                || !(configuration.isEnableRssAutoRefresh() == enableRssAutoRefresh.isSelected())
                || !(configuration.getRssRefreshPeriod() == Integer.parseInt(rssRefreshPeriod.getText()))
                || !(configuration.getPreferredView().equals(preferredView.getText()))
                || !(configuration.isEnableAuthentication() == enableAuthentication.isSelected())
                || !(configuration.getUsername().equals(username.getText()))
                || !(configuration.getPassword().equals(String.valueOf(password.getPassword())))
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
        configuration.setEnableAuthentication(enableAuthentication.isSelected());
        configuration.setUsername(username.getText());
        configuration.setPassword(String.valueOf(password.getPassword()));
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

        boolean isEnableAuthentication = configuration.isEnableAuthentication();
        enableAuthentication.setSelected(isEnableAuthentication);

        username.setText(configuration.getUsername());
        username.setEnabled(isEnableAuthentication);

        password.setText(configuration.getPassword());
        password.setEnabled(isEnableAuthentication);
    }


    public JPanel getRootPanel() {
        return rootPanel;
    }


    private void initListeners() {
        String resetPeriodValue = Integer.toString(JenkinsConfiguration.RESET_PERIOD_VALUE);

        enableJobAutoRefresh.addItemListener(new EnablerFieldListener(enableJobAutoRefresh,
                jobRefreshPeriod, resetPeriodValue));
        enableRssAutoRefresh.addItemListener(new EnablerFieldListener(enableRssAutoRefresh,
                rssRefreshPeriod, resetPeriodValue));

        enableAuthentication.addItemListener(new EnablerFieldListener(enableAuthentication,
                username, JenkinsConfiguration.RESET_STR_VALUE));
        enableAuthentication.addItemListener(new EnablerFieldListener(enableAuthentication,
                password, JenkinsConfiguration.RESET_STR_VALUE));
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
}
