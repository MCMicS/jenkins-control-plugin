package org.codinjutsu.tools.jenkins.settings;

import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Builder
@Value
class ServerSetting {
    private final @Nullable String url;
    /**
     * specified in '<a href="http://localhost:8080/jenkins/manage/">http://localhost:8080/jenkins/manage/</a>'
     */
    private final @Nullable String jenkinsUrl;
    private final @Nullable String username;
    private final @Nullable String apiToken;
    @Builder.Default
    private final boolean apiTokenModified = false;
    private final int timeout;
}
