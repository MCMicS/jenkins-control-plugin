package org.codinjutsu.tools.jenkins.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidator;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Optional;

public class ServerConfigurable implements SearchableConfigurable {

    private final Project project;
    private ServerComponent serverComponent;
    private FormValidator<JTextField> formValidator;

    public ServerConfigurable(Project project) {
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Server";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return serverComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        serverComponent = new ServerComponent();

        formValidator = FormValidator.<JTextField>init(serverComponent)
                .addValidator(serverComponent.getUsernameComponent(), component -> {
                    if (StringUtils.isNotBlank(component.getText())) {
                        String apiToken = serverComponent.getApiToken();
                        if (StringUtils.isBlank(apiToken)) {
                            throw new org.codinjutsu.tools.jenkins.exception.ConfigurationException(
                                    String.format("'%s' must be set", "API Token"));
                        }
                    }
                });
        return serverComponent.getPanel();
    }

    @Override
    public String getHelpTopic() {
        return "preferences.jenkins.servers";
    }

    @Override
    public boolean isModified() {
        final var jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        final var jenkinsSettings = JenkinsSettings.getSafeInstance(project);
        return readSettingFromUi()
                .map(serverSetting -> isModified(serverSetting, jenkinsAppSettings, jenkinsSettings))
                .orElse(false);
    }

    private @NotNull Optional<ServerSetting> readSettingFromUi() {
        if (serverComponent == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ServerSetting.builder()
                .url(serverComponent.getServerUrl())
                .username(serverComponent.getUsername())
                .apiToken(serverComponent.getApiToken())
                .apiTokenModified(serverComponent.isApiTokenModified())
                .timeout(serverComponent.getConnectionTimeout())
                .build());
    }

    public boolean isModified(ServerSetting serverSetting, JenkinsAppSettings jenkinsAppSettings,
                              JenkinsSettings jenkinsSettings) {
        boolean credentialsModified = !(jenkinsSettings.getUsername().equals(serverSetting.getUsername()))
                || serverSetting.isApiTokenModified();

        return !jenkinsAppSettings.getServerUrl().equals(serverSetting.getUrl())
                || credentialsModified
                || jenkinsSettings.getConnectionTimeout() != serverSetting.getTimeout();
    }

    @Override
    public void apply() {
        final var jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        final var jenkinsSettings = JenkinsSettings.getSafeInstance(project);
        final ServerSetting serverSetting = readSettingFromUi().get();
        serverSetting.getTimeout();
    }

    @Override
    public void reset() {
        final var jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        final var jenkinsSettings = JenkinsSettings.getSafeInstance(project);
        final ServerSetting serverSetting = readSettingFromUi().get();
        serverSetting.getTimeout();
    }

    @Override
    public void disposeUIResources() {
        serverComponent = null;
        formValidator = null;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "org.codinjutsu.tools.jenkins.servers";
    }

    @Builder
    @Value
    private static class ServerSetting {
        private final @Nullable String url;
        private final @Nullable String username;
        private final @Nullable String apiToken;
        @Builder.Default
        private final boolean apiTokenModified = false;
        private final int timeout;
    }
}
