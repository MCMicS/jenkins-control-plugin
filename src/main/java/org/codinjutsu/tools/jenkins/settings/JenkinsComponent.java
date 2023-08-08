/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBIntSpinner;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsWindowManager;
import org.codinjutsu.tools.jenkins.Version;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidator;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import javax.swing.*;
import java.util.Optional;


public class JenkinsComponent implements SearchableConfigurable {

    private final Project project;

    private @Nullable AppSettingComponent appSettingComponent;
    private @Nullable FormValidator<JBIntSpinner> formValidator;

    public JenkinsComponent(Project project) {
        this.project = project;
    }

    public JComponent createComponent() {
        if (appSettingComponent == null) {
            final var component = new AppSettingComponent();
            formValidator = FormValidator.init(component);
            setAppSettingComponent(component);
            return component.getPanel();
        }
        return appSettingComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        final var jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        return readSettingFromUi()
                .map(appSettings -> isModified(appSettings, jenkinsAppSettings))
                .orElse(false);
    }

    private boolean isModified(JenkinsAppSettings appSettingsFromUi, JenkinsAppSettings jenkinsAppSettings) {
        boolean statusToIgnoreModified =//
                appSettingsFromUi.shouldDisplaySuccessOrStable() != jenkinsAppSettings.shouldDisplaySuccessOrStable()
                        || appSettingsFromUi.shouldDisplayFailOrUnstable() != jenkinsAppSettings.shouldDisplayFailOrUnstable()
                        || appSettingsFromUi.shouldDisplayAborted() != jenkinsAppSettings.shouldDisplayAborted();

        boolean isUseGreenColor = appSettingsFromUi.isUseGreenColor() != jenkinsAppSettings.isUseGreenColor();
        boolean isShowAllInStatusbar = appSettingsFromUi.isShowAllInStatusbar() != jenkinsAppSettings.isShowAllInStatusbar();
        boolean isAutoLoadBuilds = appSettingsFromUi.isAutoLoadBuilds() != jenkinsAppSettings.isAutoLoadBuilds();
        boolean isDoubleClickActionChanged = appSettingsFromUi.getDoubleClickAction() != jenkinsAppSettings.getDoubleClickAction();
        boolean isShowLogIfTriggerBuildChanged = appSettingsFromUi.isShowLogIfTriggerBuild() != jenkinsAppSettings.isShowLogIfTriggerBuild();

        return jenkinsAppSettings.getBuildDelay() != appSettingsFromUi.getBuildDelay()
                || jenkinsAppSettings.getJobRefreshPeriod() != appSettingsFromUi.getJobRefreshPeriod()
                || jenkinsAppSettings.getRssRefreshPeriod() != appSettingsFromUi.getRssRefreshPeriod()
                || jenkinsAppSettings.getNumBuildRetries() != appSettingsFromUi.getNumBuildRetries()
                || isUseGreenColor
                || isShowAllInStatusbar
                || isAutoLoadBuilds
                || isDoubleClickActionChanged
                || isShowLogIfTriggerBuildChanged
                || statusToIgnoreModified
                || (!jenkinsAppSettings.getSuffix().equals(appSettingsFromUi.getSuffix()));
    }

    @Override
    public void disposeUIResources() {
        appSettingComponent = null;
        formValidator = null;
    }

    @Override
    public String getHelpTopic() {
        return "preferences.jenkins";
    }

    @Override
    public void apply() throws com.intellij.openapi.options.ConfigurationException {
        try {
            Optional.ofNullable(formValidator).ifPresent(FormValidator::validate);
        } catch (org.codinjutsu.tools.jenkins.exception.ConfigurationException ex) {
            throw new com.intellij.openapi.options.ConfigurationException(ex.getMessage());
        }
        readSettingFromUi().ifPresent(this::apply);
        JenkinsWindowManager.getInstance(project).ifPresent(JenkinsWindowManager::reloadConfiguration);
    }

    private void apply(JenkinsAppSettings jenkinsAppSettingsFromUi)
            throws org.codinjutsu.tools.jenkins.exception.ConfigurationException {
        final var jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);

        jenkinsAppSettings.setDelay(jenkinsAppSettingsFromUi.getBuildDelay());
        jenkinsAppSettings.setJobRefreshPeriod(jenkinsAppSettingsFromUi.getJobRefreshPeriod());
        jenkinsAppSettings.setRssRefreshPeriod(jenkinsAppSettingsFromUi.getRssRefreshPeriod());
        jenkinsAppSettings.setNumBuildRetries(jenkinsAppSettingsFromUi.getNumBuildRetries());

        jenkinsAppSettings.setDisplaySuccessOrStable(jenkinsAppSettingsFromUi.shouldDisplaySuccessOrStable());
        jenkinsAppSettings.setDisplayUnstableOrFail(jenkinsAppSettingsFromUi.shouldDisplayFailOrUnstable());
        jenkinsAppSettings.setDisplayAborted(jenkinsAppSettingsFromUi.shouldDisplayAborted());
        jenkinsAppSettings.setSuffix(jenkinsAppSettingsFromUi.getSuffix());
        jenkinsAppSettings.setUseGreenColor(jenkinsAppSettingsFromUi.isUseGreenColor());
        jenkinsAppSettings.setShowAllInStatusbar(jenkinsAppSettingsFromUi.isShowAllInStatusbar());
        jenkinsAppSettings.setAutoLoadBuilds(jenkinsAppSettingsFromUi.isAutoLoadBuilds());
        jenkinsAppSettings.setDoubleClickAction(jenkinsAppSettingsFromUi.getDoubleClickAction());
        jenkinsAppSettings.setShowLogIfTriggerBuild(jenkinsAppSettingsFromUi.isShowLogIfTriggerBuild());
    }

    @Nls
    public String getDisplayName() {
        return Version.PLUGIN_NAME;
    }

    @Override
    public void reset() {
        Optional.ofNullable(appSettingComponent).ifPresent(this::reset);
    }

    private void reset(AppSettingComponent appSettingComponentToReset) {
        final var jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);

        appSettingComponentToReset.setBuildDelay(jenkinsAppSettings.getBuildDelay());
        appSettingComponentToReset.setJobRefreshPeriod(jenkinsAppSettings.getJobRefreshPeriod());
        appSettingComponentToReset.setRssRefreshPeriod(jenkinsAppSettings.getRssRefreshPeriod());
        appSettingComponentToReset.setNumBuildRetries(jenkinsAppSettings.getNumBuildRetries());
        appSettingComponentToReset.setDoubleClickAction(jenkinsAppSettings.getDoubleClickAction());
        appSettingComponentToReset.setUseGreenColor(jenkinsAppSettings.isUseGreenColor());
        appSettingComponentToReset.setShowAllInStatusbar(jenkinsAppSettings.isShowAllInStatusbar());
        appSettingComponentToReset.setAutoLoadBuilds(jenkinsAppSettings.isAutoLoadBuilds());
        appSettingComponentToReset.setShowLogIfTriggerBuild(jenkinsAppSettings.isShowLogIfTriggerBuild());
        appSettingComponentToReset.setShouldDisplaySuccessOrStable(jenkinsAppSettings.shouldDisplaySuccessOrStable());
        appSettingComponentToReset.setShouldDisplayUnstableOrFail(jenkinsAppSettings.shouldDisplayFailOrUnstable());
        appSettingComponentToReset.setShouldDisplayAborted(jenkinsAppSettings.shouldDisplayAborted());
        appSettingComponentToReset.setReplaceWithSuffix(jenkinsAppSettings.getSuffix());
    }

    @NotNull
    @Override
    public String getId() {
        return "preferences.Jenkins";
    }

    @VisibleForTesting
    void setAppSettingComponent(@Nullable AppSettingComponent appSettingComponent) {
        this.appSettingComponent = appSettingComponent;
    }

    private @NotNull Optional<JenkinsAppSettings> readSettingFromUi() {
        return Optional.ofNullable(appSettingComponent).map(AppSettingComponent::getSetting);
    }
}
