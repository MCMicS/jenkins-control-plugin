package org.codinjutsu.tools.jenkins.logic;

import com.intellij.util.UriUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

@Getter(AccessLevel.NONE)
@Value
public class JenkinsUrlMapper implements UnaryOperator<String> {
    private final @NotNull String serverUrl;
    private final @NotNull String jenkinsUrl;

    public JenkinsUrlMapper(@NotNull String serverUrl, @NotNull String jenkinsUrl) {
        this.serverUrl = UriUtil.trimTrailingSlashes(serverUrl);
        this.jenkinsUrl = UriUtil.trimTrailingSlashes(jenkinsUrl);
    }

    @Override
    public String apply(@Nullable String urlFromServer) {
        if (urlFromServer == null) {
            return null;
        }
        return urlFromServer.replace(jenkinsUrl, serverUrl);
    }
}
