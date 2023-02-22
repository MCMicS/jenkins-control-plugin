package org.codinjutsu.tools.jenkins.exception;

import org.codinjutsu.tools.jenkins.model.Build;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

public class NoBuildFoundException extends JenkinsPluginRuntimeException {

    public NoBuildFoundException(@NotNull Build build) {
        super(createMessage(build, null));
    }

    public NoBuildFoundException(@NotNull Build build, Throwable throwable) {
        super(createMessage(build, throwable.getMessage()), throwable);
    }

    @NotNull
    private static String createMessage(@NotNull Build build, @Nullable String cause) {
        final String buildName = build.getNameToRender();
        if (cause == null) {
            return MessageFormat.format("Could not find Build data for: {0} [{1}].", buildName, build.getUrl());
        }
        return MessageFormat.format("Could not find Build data for: {0} [{1}].\nCause: {2}",
                buildName, build.getUrl(), cause);
    }
}
