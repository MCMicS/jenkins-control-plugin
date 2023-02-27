package org.codinjutsu.tools.jenkins.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.ConfigurationValidator;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.security.JenkinsVersion;
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
        serverComponent = new ServerComponent(this::testConnection);

        formValidator = FormValidator.<JTextField>init(serverComponent)
                .addValidator(serverComponent.getUsernameComponent(), component -> {
                    if (StringUtils.isNotBlank(component.getText())) {
                        String apiToken = serverComponent.getApiToken();
                        if (serverComponent.isApiTokenModified() && StringUtils.isBlank(apiToken)) {
                            throw new org.codinjutsu.tools.jenkins.exception.ConfigurationException(
                                    String.format("'%s' must be set", "API Token"));
                        }
                    }
                });
        return serverComponent.getPanel();
    }

    private ConfigurationValidator.@NotNull ValidationResult testConnection(@NotNull ServerSetting serverSetting) {
        final var apiToken = serverSetting.isApiTokenModified() ? serverSetting.getApiToken() ://
                JenkinsSettings.getSafeInstance(project).getPassword();
        final String serverUrl = Optional.ofNullable(serverSetting.getUrl()).orElse("");
        final var jenkinsUrl = RequestManager.getInstance(project).testAuthenticate(serverUrl,
                serverSetting.getUsername(), apiToken, "", JenkinsVersion.VERSION_2,
                serverSetting.getTimeout());
        if (StringUtils.isEmpty(jenkinsUrl)) {
            throw new ConfigurationException("Cannot find 'Jenkins URL'. Please check your Jenkins Location");
        }
        return ConfigurationValidator.getInstance(project).validate(serverUrl, jenkinsUrl);
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
        return Optional.ofNullable(serverComponent).map(ServerComponent::getServerSetting);
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
    public void apply() throws com.intellij.openapi.options.ConfigurationException {
        try {
            Optional.ofNullable(formValidator).ifPresent(FormValidator::validate);
        } catch (org.codinjutsu.tools.jenkins.exception.ConfigurationException ex) {
            throw new com.intellij.openapi.options.ConfigurationException(ex.getMessage());
        }
        readSettingFromUi().ifPresent(this::apply);
    }

    private void apply(ServerSetting serverSetting) throws ConfigurationException {
        final var jenkinsServerSetting = JenkinsAppSettings.getSafeInstance(project);
        final var jenkinsSettings = JenkinsSettings.getSafeInstance(project);
        if (!StringUtils.equals(jenkinsServerSetting.getServerUrl(), serverSetting.getUrl())) {
            jenkinsSettings.clearFavoriteJobs();
            jenkinsSettings.setLastSelectedView(null);
        }

        jenkinsServerSetting.setServerUrl(serverSetting.getUrl());
        jenkinsSettings.setUsername(serverSetting.getUsername());
        if (serverSetting.isApiTokenModified()) {
            jenkinsSettings.setPassword(serverSetting.getApiToken());
            Optional.ofNullable(serverComponent).ifPresent(ServerComponent::resetApiTokenModified);
        }
        jenkinsSettings.setConnectionTimeout(serverSetting.getTimeout());
    }

    @Override
    public void reset() {
        Optional.ofNullable(serverComponent).ifPresent(this::reset);
    }

    public void reset(ServerComponent serverComponentToReset) {
        final var jenkinsServerSetting = JenkinsAppSettings.getSafeInstance(project);
        final var jenkinsSettings = JenkinsSettings.getSafeInstance(project);

        serverComponentToReset.setServerUrl(jenkinsServerSetting.getServerUrl());
        final var username = jenkinsSettings.getUsername();
        serverComponentToReset.setUsername(username);
        if (StringUtils.isNotBlank(username)) {
            serverComponentToReset.setApiToken(jenkinsSettings.getPassword());
            serverComponentToReset.resetApiTokenModified();
        }
        serverComponentToReset.setConnectionTimeout(jenkinsSettings.getConnectionTimeout());
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

}
