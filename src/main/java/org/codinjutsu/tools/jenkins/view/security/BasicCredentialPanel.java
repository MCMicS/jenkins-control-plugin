package org.codinjutsu.tools.jenkins.view.security;

import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.view.validator.NotNullValidator;

import javax.swing.*;
import java.awt.*;

public class BasicCredentialPanel extends JPanel {

    private JPanel rootPanel;

    private JTextField usernameField;
    private JPasswordField passwordField;

    public BasicCredentialPanel() {
        super(new BorderLayout());
        add(rootPanel);

        setName("basicCredentialPanel");
        usernameField.setName("username field");
        passwordField.setName("password field");

    }

    public void updateFields(String username, String password) {
        usernameField.setText(username);
        passwordField.setText(password);
    }

    public void resetFields() {
        usernameField.setText("");
        passwordField.setText("");
    }


    public void validateInputs() throws ConfigurationException {
        new NotNullValidator().validate(usernameField);
        new NotNullValidator().validate(passwordField);
    }

    public String getUsernameValue() {
        return usernameField.getText();
    }

    public String getPasswordValue() {
        return String.valueOf(passwordField.getPassword());
    }
}
