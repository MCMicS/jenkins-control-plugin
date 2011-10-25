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
