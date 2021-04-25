package org.codinjutsu.tools.jenkins.exception;

public class JenkinsPluginRuntimeException extends RuntimeException {

    public JenkinsPluginRuntimeException(String message) {
        super(message);
    }

    public JenkinsPluginRuntimeException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
