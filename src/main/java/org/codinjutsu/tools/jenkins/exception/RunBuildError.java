package org.codinjutsu.tools.jenkins.exception;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

public class RunBuildError extends JenkinsPluginRuntimeException {

    public RunBuildError(@NotNull @NonNls String errorMessage) {
        super(createMessage(errorMessage));
    }

    @NotNull
    private static String createMessage(@NotNull @NonNls String errorMessage) {
        return MessageFormat.format("Error during trigger Build: {0}", errorMessage);
    }
}
