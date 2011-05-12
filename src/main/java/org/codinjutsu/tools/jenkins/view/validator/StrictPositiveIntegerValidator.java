package org.codinjutsu.tools.jenkins.view.validator;

import org.codinjutsu.tools.jenkins.exception.ConfigurationException;

import javax.swing.*;

public class StrictPositiveIntegerValidator implements UIValidator<JTextField> {
    public void validate(JTextField component) throws ConfigurationException {
        String value = component.getText();
        if (component.isEnabled() && !"".equals(value)) {    //TODO A revoir
            try {
                int intValue = Integer.parseInt(value);
                if (intValue <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                throw new ConfigurationException("'" + value + "' is not a positive integer");
            }
        }
    }
}
