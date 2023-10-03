package org.codinjutsu.tools.jenkins.settings;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import org.codinjutsu.tools.jenkins.JenkinsControlBundle;
import org.codinjutsu.tools.jenkins.exception.AuthenticationException;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.StringUtil;
import org.codinjutsu.tools.jenkins.view.action.ActionUtil;
import org.codinjutsu.tools.jenkins.view.action.ReloadConfigurationAction;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidationPanel;
import org.codinjutsu.tools.jenkins.view.annotation.GuiField;
import org.codinjutsu.tools.jenkins.view.validator.NotNullValidator;
import org.codinjutsu.tools.jenkins.view.validator.PositiveIntegerValidator;
import org.codinjutsu.tools.jenkins.view.validator.UrlValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.util.Optional;

import static javax.swing.SwingConstants.LEFT;
import static org.codinjutsu.tools.jenkins.util.GuiUtil.simplePanel;
import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.POSITIVE_INTEGER;
import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.URL;

public class ServerComponent implements FormValidationPanel {
    private final JPanel mainPanel;
    @GuiField(validators = URL)
    private final JBTextField serverUrl = new JBTextField();
    @GuiField(validators = URL)
    private final JBTextField jenkinsUrl = new JBTextField();
    private final JBTextField username = new JBTextField();
    private final JBPasswordField apiToken = new JBPasswordField();
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner connectionTimeout = new JBIntSpinner(10, 5, 300);
    private final JButton testConnection = new JButton(JenkinsControlBundle.message("settings.server.test_connection"));
    private final JLabel connectionStatusLabel = new JLabel();
    private final JTextPane debugTextPane = createDebugTextPane();
    private final JPanel debugPanel = JBUI.Panels.simplePanel(debugTextPane);
    private boolean apiTokenModified;

    public ServerComponent(ServerConnectionValidator serverConnectionValidator) {
        apiToken.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setApiTokenModified(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setApiTokenModified(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setApiTokenModified(true);
            }
        });
        final JBDimension size = JBUI.size(150, username.getPreferredSize().height);
        username.setPreferredSize(size);
        username.setHorizontalAlignment(LEFT);
        connectionStatusLabel.setFont(connectionStatusLabel.getFont().deriveFont(Font.BOLD));
        final var reloadConfiguration = new JButton(JenkinsControlBundle.message("action.Jenkins.ReloadConfiguration.text"));
        reloadConfiguration.addActionListener(event -> reloadConfiguration(DataManager.getInstance().getDataContext(reloadConfiguration)));

        testConnection.addActionListener(event -> testConnection(serverConnectionValidator));
        debugPanel.setVisible(false);
        debugPanel.setBorder(IdeBorderFactory.createTitledBorder(//
                JenkinsControlBundle.message("settings.server.debugInfo"), false,//
                JBUI.insetsTop(8)).setShowLine(false));
        jenkinsUrl.getEmptyText().setText(JenkinsControlBundle.message("settings.server.jenkinsUrl.useServerAddress"));
        jenkinsUrl.setToolTipText(JenkinsControlBundle.message("settings.server.jenkinsUrl.tooltip"));

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JenkinsControlBundle.message("settings.server.address"), serverUrl)
                .addLabeledComponent(JenkinsControlBundle.message("settings.server.jenkinsUrl"), jenkinsUrl)
                .addLabeledComponent(new JBLabel(JenkinsControlBundle.message("settings.server.username")),
                        username)
                .addLabeledComponent(JenkinsControlBundle.message("settings.server.api_token"), apiToken)
                .addLabeledComponent(JenkinsControlBundle.message("settings.server.connection_timeout"),
                        createConnectionTimeout())
                .addComponentToRightColumn(reloadConfiguration)
                .addComponentToRightColumn(createTestConnectionPanel())
                .addComponent(debugPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private void reloadConfiguration(@NotNull DataContext dataContext) {
        Optional.ofNullable(ActionManager.getInstance().getAction(ReloadConfigurationAction.ACTION_ID))
                .ifPresent(action -> ActionUtil.performAction(action, "ServerSetting", dataContext));
    }

    private static @NotNull JTextPane createDebugTextPane() {
        final var textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(JBColor.WHITE);
        final HTMLEditorKit simple = new HTMLEditorKit();
        textPane.setEditorKit(simple);
        textPane.getDocument().putProperty("IgnoreCharsetDirective", Boolean.valueOf(true));
        return textPane;
    }

    private void testConnection(ServerConnectionValidator serverConnectionValidator) {
        try {
            new NotNullValidator().validate(serverUrl);
            new UrlValidator().validate(serverUrl);
            new UrlValidator().validate(jenkinsUrl);
            new PositiveIntegerValidator().validate(connectionTimeout);
            debugPanel.setVisible(false);
            final var serverSetting = getServerSetting();
            final var validationResult = serverConnectionValidator.validateConnection(serverSetting);
            if (validationResult.isValid()) {
                setConnectionFeedbackLabel(JBColor.GREEN,//
                        JenkinsControlBundle.message("settings.server.test_connection.successful"));
                if (serverSetting.isApiTokenModified()) {
                    setApiToken(serverSetting.getApiToken());
                }
            } else {
                setConnectionFeedbackLabel(JBColor.RED,//
                        JenkinsControlBundle.message("settings.server.test_connection.invalidConfiguration"));
                debugPanel.setVisible(true);
                debugTextPane.setText(String.join("<br>", validationResult.getErrors()));
            }
        } catch (AuthenticationException authenticationException) {
            setConnectionFeedbackLabel(authenticationException);
            final var responseBody = authenticationException.getResponseBody();
            if (StringUtil.isNotBlank(responseBody)) {
                debugPanel.setVisible(true);
                debugTextPane.setText(responseBody);
            }
        } catch (Exception ex) {
            setConnectionFeedbackLabel(ex);
        }
    }

    private void setConnectionFeedbackLabel(@NotNull Exception cause) {
        setConnectionFeedbackLabel(JBColor.RED,//
                JenkinsControlBundle.message("settings.server.test_connection.fail", cause.getMessage()));
    }

    private void setConnectionFeedbackLabel(final Color labelColor, final String labelText) {
        GuiUtil.runInSwingThread(() -> {
            connectionStatusLabel.setForeground(labelColor);
            connectionStatusLabel.setText(labelText);
        });
    }

    private @NotNull JPanel createConnectionTimeout() {
        return GuiUtil.createLabeledComponent(connectionTimeout, JenkinsControlBundle.message("settings.seconds"));
    }

    private @NotNull JPanel createTestConnectionPanel() {
        return simplePanel(testConnection, connectionStatusLabel);
    }

    public @NotNull JPanel getPanel() {
        return mainPanel;
    }

    public @Nullable JComponent getServerUrlComponent() {
        return serverUrl;
    }

    public @NotNull ServerSetting getServerSetting() {
        final String usernameForSetting = getUsername();
        return ServerSetting.builder()
                .url(getServerUrl())
                .jenkinsUrl(getJenkinsUrl())
                .username(StringUtil.isBlank(usernameForSetting) ? "" : usernameForSetting)
                .apiToken(getApiToken())
                .apiTokenModified(isApiTokenModified())
                .timeout(getConnectionTimeout())
                .build();
    }

    public @NotNull String getServerUrl() {
        return serverUrl.getText();
    }

    public void setServerUrl(@NotNull String serverUrlToSet) {
        serverUrl.setText(serverUrlToSet);
    }

    private @NotNull String getJenkinsUrl() {
        return jenkinsUrl.getText();
    }

    public void setJenkinsUrl(@Nullable String jenkinsUrlToSet) {
        jenkinsUrl.setText(jenkinsUrlToSet);
    }

    public @NotNull String getUsername() {
        return username.getText();
    }

    public void setUsername(@NotNull String usernameToSet) {
        username.setText(usernameToSet);
    }

    public @NotNull JTextField getUsernameComponent() {
        return username;
    }

    public @NotNull String getApiToken() {
        return String.valueOf(apiToken.getPassword());
    }

    public void setApiToken(@Nullable String apiTokenToSet) {
        apiToken.setPasswordIsStored(StringUtil.isNotBlank(apiTokenToSet));
    }

    @VisibleForTesting
    void setApiTokenValue(@Nullable String apiTokenToSet) {
        setApiToken(apiTokenToSet);
        apiToken.setText(apiTokenToSet);
        setApiTokenModified(true);
    }

    public int getConnectionTimeout() {
        return connectionTimeout.getNumber();
    }

    public void setConnectionTimeout(int timeout) {
        connectionTimeout.setNumber(timeout);
    }

    public boolean isApiTokenModified() {
        return apiTokenModified;
    }

    public void resetApiTokenModified() {
        setApiTokenModified(false);
    }

    private void setApiTokenModified(boolean apiTokenModified) {
        this.apiTokenModified = apiTokenModified;
    }
}
