package org.codinjustu.tools.jenkins.view.validator;

import org.codinjustu.tools.jenkins.exception.ConfigurationException;

import javax.swing.*;

public class NotNullValidator implements UIValidator<JTextField> {
    public void validate(JTextField component) throws ConfigurationException {
        if (component.isEnabled()) {    //TODO a revoir
            String value = component.getText();
            if (value == null || "".equals(value)) {
                throw new ConfigurationException("'" + component.getName() + "' must be set");
            }
        }
    }
}
