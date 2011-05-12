package org.codinjutsu.tools.jenkins.view.validator;

import org.codinjutsu.tools.jenkins.exception.ConfigurationException;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlValidator implements UIValidator<JTextField> {
    public void validate(JTextField component) throws ConfigurationException {
        String value = component.getText();
        try {
            new URL(value);
        } catch (MalformedURLException ex) {
            throw new ConfigurationException("URL '" + value + "' is malformed");
        }
    }
}
