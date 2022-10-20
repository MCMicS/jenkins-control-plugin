package org.codinjutsu.tools.jenkins.settings;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
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
import org.codinjutsu.tools.jenkins.view.validator.NotNullValidator;
import org.codinjutsu.tools.jenkins.view.validator.PositiveIntegerValidator;
import org.codinjutsu.tools.jenkins.view.validator.UrlValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

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
    private final JButton testConnection = new JButton(JenkinsControlBundle.message("settings.server.test_connection"));
    private final JLabel connectionStatusLabel = new JLabel();
    private final JTextPane debugTextPane = createDebugTextPane();
    private final JPanel debugPanel = JBUI.Panels.simplePanel(debugTextPane);
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
        final JBDimension size = JBUI.size(150, username.getPreferredSize().height);
        username.setPreferredSize(size);
        username.setHorizontalAlignment(JBTextField.LEFT);

        testConnection.addActionListener(event -> testConnection());
        debugPanel.setVisible(false);
        debugPanel.setBorder(IdeBorderFactory.createTitledBorder(//
                JenkinsControlBundle.message("settings.server.debugInfo"), false,//
                JBUI.insetsTop(8)).setShowLine(false));
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JenkinsControlBundle.message("settings.server.address"), serverUrl)
                .addLabeledComponent(new JBLabel(JenkinsControlBundle.message("settings.server.username")),
                        username)
                .addLabeledComponent(JenkinsControlBundle.message("settings.server.api_token"), apiToken)
                .addLabeledComponent(JenkinsControlBundle.message("settings.server.connection_timeout"), createConnectionTimeout())
                .addComponentToRightColumn(createTestConnectionPanel())
                .addComponent(debugPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private static @NotNull JTextPane createDebugTextPane() {
        final var textPane = new JTextPane();
        final var htmlEditorKit = new HTMLEditorKit();
        final var htmlDocument = new HTMLDocument();

        textPane.setEditable(false);
        textPane.setBackground(JBColor.WHITE);
        textPane.setEditorKit(htmlEditorKit);
        htmlEditorKit.install(textPane);
        textPane.setDocument(htmlDocument);
        return textPane;
    }

    private void testConnection() {
        try {
            new NotNullValidator().validate(serverUrl);
            new UrlValidator().validate(serverUrl);
            new PositiveIntegerValidator().validate(connectionTimeout);
            connectionStatusLabel.setText("Success");
            debugPanel.setVisible(false);
        } catch (Exception ex) {
            //throw new ConfigurationException(ex.getMessage());
            connectionStatusLabel.setText(ex.getMessage());
            debugPanel.setVisible(true);
            debugTextPane.setText(ex.getMessage());
        }
    }

    private @NotNull JPanel createConnectionTimeout() {
        final var panel = JBUI.Panels.simplePanel(new JBLabel(JenkinsControlBundle.message("settings.seconds")));
        panel.addToLeft(connectionTimeout);
        return panel;
    }

    private @NotNull JPanel createTestConnectionPanel() {
        final var panel = JBUI.Panels.simplePanel(connectionStatusLabel);
        panel.addToLeft(this.testConnection);
        return panel;
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
