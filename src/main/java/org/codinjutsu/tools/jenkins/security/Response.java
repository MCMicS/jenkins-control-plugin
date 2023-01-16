package org.codinjutsu.tools.jenkins.security;

import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Value
public class Response {

    private final int statusCode;
    private final @Nullable String data;

    private final @Nullable String error;
}
