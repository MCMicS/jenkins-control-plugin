package org.codinjutsu.tools.jenkins.settings;

import org.codinjutsu.tools.jenkins.exception.AuthenticationException;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.ConfigurationValidator;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ServerConnectionValidator {
    ConfigurationValidator.@NotNull ValidationResult validateConnection(@NotNull ServerSetting serverSetting)
            throws AuthenticationException, ConfigurationException;
}
