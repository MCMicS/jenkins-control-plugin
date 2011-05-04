package org.codinjustu.tools.jenkins.view.validator;

import org.codinjustu.tools.jenkins.exception.ConfigurationException;

import javax.swing.*;

public interface UIValidator<T extends JComponent> {
    void validate(T component) throws ConfigurationException;
}
