package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.components.Service;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.UnaryOperator;

@Service
final class UrlMapperService {

    public @NotNull UnaryOperator<String> getMapper(@NotNull JenkinsSettings jenkinsSettings, @NotNull String serverUrl) {
        return Optional.of(jenkinsSettings.getJenkinsUrl())
                .filter(StringUtils::isNotEmpty)
                .map(jenkinsUrl -> getMapper(jenkinsUrl, serverUrl))
                .orElseGet(UnaryOperator::identity);
    }

    public @NotNull UnaryOperator<String> getMapper(@NotNull String jenkinsUrl, @NotNull String serverUrl) {
        final boolean useDefault = StringUtils.isEmpty(jenkinsUrl) || StringUtils.equalsIgnoreCase(jenkinsUrl, serverUrl);
        return useDefault ? UnaryOperator.identity() :
                new JenkinsUrlMapper(serverUrl, jenkinsUrl);
    }
}
