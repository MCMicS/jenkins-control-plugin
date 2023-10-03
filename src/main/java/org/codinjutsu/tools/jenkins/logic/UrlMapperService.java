package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.util.text.StringUtil;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.UnaryOperator;

@Service
final class UrlMapperService {

    public @NotNull UnaryOperator<String> getMapper(@NotNull JenkinsSettings jenkinsSettings, @NotNull String serverUrl) {
        return Optional.of(jenkinsSettings.getJenkinsUrl())
                .filter(StringUtil::isNotEmpty)
                .map(jenkinsUrl -> getMapper(jenkinsUrl, serverUrl))
                .orElseGet(UnaryOperator::identity);
    }

    public @NotNull UnaryOperator<String> getMapper(@NotNull String jenkinsUrl, @NotNull String serverUrl) {
        final boolean useDefault = StringUtil.isEmpty(jenkinsUrl) || StringUtil.equalsIgnoreCase(jenkinsUrl, serverUrl);
        return useDefault ? UnaryOperator.identity() :
                new JenkinsUrlMapper(serverUrl, jenkinsUrl);
    }
}
