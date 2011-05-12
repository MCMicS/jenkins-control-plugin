package org.codinjutsu.tools.jenkins.view.validator;

import org.codinjutsu.tools.jenkins.exception.ConfigurationException;

import javax.swing.*;

public class PositiveIntegerValidator implements UIValidator<JTextField> {
    public void validate(JTextField component) throws ConfigurationException {
        String value = component.getText();
        if (component.isEnabled() && !"".equals(value)) {    //TODO A revoir
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                throw new ConfigurationException("'" + value + "' is not a positive integer");
            }
        }
    }
}