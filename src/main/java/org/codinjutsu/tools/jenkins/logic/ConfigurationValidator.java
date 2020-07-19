package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
public class ConfigurationValidator {
    @NotNull
    private final UrlBuilder urlBuilder;

    public ConfigurationValidator(@NotNull Project project) {
        this.urlBuilder = UrlBuilder.getInstance(project);
    }

    public static ConfigurationValidator getInstance(Project project) {
        return Optional.ofNullable(ServiceManager.getService(project, ConfigurationValidator.class))
                .orElseGet(() -> new ConfigurationValidator(project));
    }

    @NotNull
    public ValidationResult validate(@NotNull String configuredServerUrl, @NotNull String jenkinsUrlAsString) {
        final URL configuredUrl = urlBuilder.toUrl(configuredServerUrl);
        final URL jenkinsUrl = urlBuilder.toUrl(jenkinsUrlAsString);
        final URL configureUrl = urlBuilder.createConfigureUrl(configuredServerUrl);

        final ValidationResult.ValidationResultBuilder validationResult = ValidationResult.builder();
        if (configuredUrl != null && jenkinsUrl != null) {
            validateProtocol(validationResult, configuredUrl, jenkinsUrl, configureUrl);
            validateHost(validationResult, configuredUrl, jenkinsUrl, configureUrl);
            validatePort(validationResult, configuredUrl, jenkinsUrl, configureUrl);
            validatePath(validationResult, configuredUrl, jenkinsUrl, configureUrl);
        }
        return validationResult.build();
    }

    @SuppressWarnings("java:S1854")
    private void validatePort(@NotNull ValidationResult.ValidationResultBuilder validationResult,
                              @NotNull URL configuration, @NotNull URL jenkins, @NotNull URL configureUrl) {
        final int configuredPort = configuration.getPort();
        final int jenkinsPort = jenkins.getPort();

        if (isPortSet(configuredPort) != isPortSet(jenkinsPort) || configuredPort != jenkinsPort) {
            validationResult.error(String.format("Jenkins Server Port Mismatch: expected='%s' - actual='%s'. Look at the value of 'Jenkins URL' at %s",
                    getPortString(configuredPort), getPortString(jenkinsPort), configureUrl));
        }
    }

    private void validateHost(@NotNull ValidationResult.ValidationResultBuilder validationResult,
                              @NotNull URL configuration, @NotNull URL jenkins, @NotNull URL configureUrl) {
        if (!StringUtils.equals(configuration.getHost(), jenkins.getHost())) {
            validationResult.error(String.format("Jenkins Server Host Mismatch: expected='%s' - actual='%s'. Look at the value of 'Jenkins URL' at %s",
                    configuration.getHost(), jenkins.getHost(), configureUrl));
        }
    }

    private void validateProtocol(@NotNull ValidationResult.ValidationResultBuilder validationResult,
                              @NotNull URL configuration, @NotNull URL jenkins, @NotNull URL configureUrl) {
        if (!StringUtils.equals(configuration.getProtocol(), jenkins.getProtocol())) {
            validationResult.error(String.format("Jenkins Server Protocol Mismatch: expected='%s' - actual='%s'. Look at the value of 'Jenkins URL' at %s",
                    configuration.getProtocol(), jenkins.getProtocol(), configureUrl));
        }
    }

    private void validatePath(@NotNull ValidationResult.ValidationResultBuilder validationResult,
                              @NotNull URL configuration, @NotNull URL jenkins, @NotNull URL configureUrl) {
        if (!isSameUrlPath(configuration.getPath(), jenkins.getPath())) {
            validationResult.error(String.format("Jenkins Server Path Mismatch: expected='%s' - actual='%s'. Look at the value of 'Jenkins URL' at %s",
                    configuration.getPath(), jenkins.getPath(), configureUrl));
        }
    }

    private boolean isSameUrlPath(@NotNull String leftUrl, @NotNull String rightUrl) {
        return StringUtils.equals(urlBuilder.removeTrailingSlash(leftUrl), urlBuilder.removeTrailingSlash(rightUrl));
    }

    @NotNull
    private String getPortString(int port) {
        return isPortSet(port) ? String.valueOf(port) : "unset";
    }

    private boolean isPortSet(int port) {
        return port != -1;
    }

    @Builder
    @Value
    public static final class ValidationResult {

        @Singular
        @NotNull
        private final List<String> errors;

        public boolean isValid() {
            return errors.isEmpty();
        }

        @NotNull
        public String getFirstError() {
            return errors.isEmpty() ? "" : errors.get(0);
        }

    }
}
