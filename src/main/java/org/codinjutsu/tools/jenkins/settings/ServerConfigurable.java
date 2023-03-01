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
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.Optional;

public class ServerConfigurable implements SearchableConfigurable {

    private final Project project;
    private @Nullable ServerComponent serverComponent;
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
    public @Nullable JComponent getPreferredFocusedComponent() {
        return serverComponent == null ? null : serverComponent.getServerUrlComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        final var serverComponentToSet = new ServerComponent(this::testConnection);

        formValidator = FormValidator.<JTextField>init(serverComponentToSet)
                .addValidator(serverComponentToSet.getUsernameComponent(), component -> {
                    if (StringUtils.isNotBlank(component.getText())) {
                        String apiToken = serverComponentToSet.getApiToken();
                        if (serverComponentToSet.isApiTokenModified() && StringUtils.isBlank(apiToken)) {
                            throw new org.codinjutsu.tools.jenkins.exception.ConfigurationException(
                                    String.format("'%s' must be set", "API Token"));
                        }
                    }
                });
        setServerComponent(serverComponentToSet);
        return serverComponentToSet.getPanel();
    }

    @VisibleForTesting
    void setServerComponent(@Nullable ServerComponent serverComponent) {
        this.serverComponent = serverComponent;
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

    public static boolean isModified(ServerSetting serverSetting, JenkinsAppSettings jenkinsAppSettings,
                              JenkinsSettings jenkinsSettings) {
        boolean credentialsModified = !(jenkinsSettings.getUsername().equals(serverSetting.getUsername()))
                || serverSetting.isApiTokenModified();

        final var differentJenkinsInstance = !jenkinsAppSettings.getServerUrl().equals(serverSetting.getUrl())
                || !jenkinsSettings.getJenkinsUrl().equals(serverSetting.getJenkinsUrl());
        return differentJenkinsInstance
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
        jenkinsSettings.setJenkinsUrl(serverSetting.getJenkinsUrl());
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

    private void reset(ServerComponent serverComponentToReset) {
        final var jenkinsServerSetting = JenkinsAppSettings.getSafeInstance(project);
        final var jenkinsSettings = JenkinsSettings.getSafeInstance(project);

        serverComponentToReset.setServerUrl(jenkinsServerSetting.getServerUrl());
        serverComponentToReset.setJenkinsUrl(jenkinsSettings.getJenkinsUrl());
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
