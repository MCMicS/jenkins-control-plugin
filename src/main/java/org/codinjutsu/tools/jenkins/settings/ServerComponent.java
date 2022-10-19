package org.codinjutsu.tools.jenkins.settings;

import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsControlBundle;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidationPanel;
import org.codinjutsu.tools.jenkins.view.annotation.GuiField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.POSITIVE_INTEGER;
import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.URL;

public class ServerComponent implements FormValidationPanel {
    private final JPanel mainPanel;
    @GuiField(validators = URL)
    private final JBTextField serverUrl = new JBTextField();
    private final JBTextField username = new JBTextField();
    private final JBPasswordField apiToken = new JBPasswordField();
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner connectionTimeout = new JBIntSpinner(10, 5, 300);
    private final JButton testConnection = new JButton("Test Connection");
    private boolean apiTokenModified;

    public ServerComponent() {
        apiToken.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                apiTokenModified = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                apiTokenModified = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                apiTokenModified = true;
            }
        });
        //apiToken.setPreferredSize(TEXT_FIELD_WIDTH);
        final JBDimension size = JBUI.size(150, username.getPreferredSize().height);
        username.setPreferredSize(size);
//        username.setSize(size);
//        username.setMaximumSize(size);
        username.setHorizontalAlignment(JBTextField.LEFT);
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JenkinsControlBundle.message("settings.server.address"), serverUrl)
                .addLabeledComponent(new JBLabel(JenkinsControlBundle.message("settings.server.username")),
                        username)
                .addComponentToRightColumn(new JBLabel("Test"))
                .addLabeledComponent(JenkinsControlBundle.message("settings.server.api_token"), apiToken)
                .addLabeledComponent(JenkinsControlBundle.message("settings.server.connection_timeout"),
                        connectionTimeout)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public @NotNull JPanel getPanel() {
        return mainPanel;
    }

    public @NotNull JComponent getPreferredFocusedComponent() {
        return serverUrl;
    }

    public @NotNull String getServerUrl() {
        return serverUrl.getText();
    }

    public void setServerUrl(@NotNull String serverUrlToSet) {
        serverUrl.setText(serverUrlToSet);
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
        apiToken.setPasswordIsStored(StringUtils.isNotBlank(apiTokenToSet));
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
        apiTokenModified = false;
    }
}
