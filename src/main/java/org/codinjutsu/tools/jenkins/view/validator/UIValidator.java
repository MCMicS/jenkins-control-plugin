package org.codinjutsu.tools.jenkins.view.validator;

import org.codinjutsu.tools.jenkins.exception.ConfigurationException;

import javax.swing.*;

public interface UIValidator<T extends JComponent> {
    void validate(T component) throws ConfigurationException;
}
