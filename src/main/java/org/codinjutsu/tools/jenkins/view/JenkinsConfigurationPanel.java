package org.codinjutsu.tools.jenkins.view;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.action.ThreadFunctor;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.AuthenticationResult;
import org.codinjutsu.tools.jenkins.logic.JenkinsRequestManager;
import org.codinjutsu.tools.jenkins.util.SwingUtils;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidator;
import org.codinjutsu.tools.jenkins.view.annotation.GuiField;
import org.codinjutsu.tools.jenkins.view.validator.NotNullValidator;
import org.codinjutsu.tools.jenkins.view.validator.UIValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.*;

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

    private FormValidator formValidator;


    public JenkinsConfigurationPanel(final JenkinsRequestManager jenkinsRequestManager) {
        this(jenkinsRequestManager, true);
    }

    JenkinsConfigurationPanel(final JenkinsRequestManager jenkinsRequestManager, boolean installBrowserFileBrowser) {
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

        testConnexionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final AuthenticationResult authResult = jenkinsRequestManager.testConnexion(
                        serverUrl.getText(),
                        enableAuthentication.isSelected(), username.getText(), passwordFile.getComponent().getText());


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
    }


    public boolean isModified(JenkinsConfiguration configuration) {
        return !configuration.getServerUrl().equals(serverUrl.getText())
                || !(configuration.getBuildDelay() == Integer.parseInt(buildDelay.getText()))
                || !(configuration.isEnableJobAutoRefresh() == enableJobAutoRefresh.isSelected())
                || !(configuration.getJobRefreshPeriod() == Integer.parseInt(jobRefreshPeriod.getText()))
                || !(configuration.isEnableRssAutoRefresh() == enableRssAutoRefresh.isSelected())
                || !(configuration.getRssRefreshPeriod() == Integer.parseInt(rssRefreshPeriod.getText()))
                || !(configuration.getPreferredView().equals(preferredView.getText()))
                || !(configuration.isEnableAuthentication() == enableAuthentication.isSelected())
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
        configuration.setEnableAuthentication(enableAuthentication.isSelected());
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

        boolean isEnableAuthentication = configuration.isEnableAuthentication();
        enableAuthentication.setSelected(isEnableAuthentication);

        username.setText(configuration.getUsername());
        username.setEnabled(isEnableAuthentication);

        passwordFile.getComponent().setText(configuration.getPasswordFile());
        passwordFile.setEnabled(isEnableAuthentication);
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
        enableAuthentication.addItemListener(new EnablerPasswordFileListener(enableAuthentication,
                passwordFile, JenkinsConfiguration.RESET_STR_VALUE));


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
