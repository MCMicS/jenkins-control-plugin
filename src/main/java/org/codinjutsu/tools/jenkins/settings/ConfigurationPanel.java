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

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.NumberDocument;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.DoubleClickAction;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.view.DoubleClickActionRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Optional;

public class ConfigurationPanel {

    private JTextField buildDelay;
    private JTextField jobRefreshPeriod;
    private JTextField rssRefreshPeriod;
    private JTextField numBuildRetries;
    @Getter
    private JPanel rootPanel;
    private JPanel rssStatusFilterPanel;
    private JCheckBox successOrStableCheckBox;
    private JCheckBox unstableOrFailCheckBox;
    private JCheckBox abortedCheckBox;
    private JPanel uploadPatchSettingsPanel;
    private JTextField replaceWithSuffix;
    private JCheckBox showAllInStatusbar;
    private JCheckBox useGreenColor;
    private JCheckBox autoLoadBuildsOnFirstLevel;
    private JComboBox<DoubleClickAction> doubleClickAction;
    private JCheckBox showLogIfTriggerBuild;

    public ConfigurationPanel() {
        buildDelay.setName("buildDelay");
        jobRefreshPeriod.setName("job refresh period");
        rssRefreshPeriod.setName("rssRefreshPeriod");
        numBuildRetries.setName("numBuildRetries");

        successOrStableCheckBox.setName("successOrStableCheckBox");
        unstableOrFailCheckBox.setName("unstableOrFailCheckBox");
        abortedCheckBox.setName("abortedCheckBox");

        rssStatusFilterPanel.setBorder(IdeBorderFactory.createTitledBorder("Event Log Settings", true));

        buildDelay.setDocument(new NumberDocument());
        jobRefreshPeriod.setDocument(new NumberDocument());
        rssRefreshPeriod.setDocument(new NumberDocument());
        numBuildRetries.setDocument(new NumberDocument());

        uploadPatchSettingsPanel.setBorder(IdeBorderFactory.createTitledBorder("Upload a Patch Settings", true));
        doubleClickAction.setEditable(false);
        doubleClickAction.setRenderer(new DoubleClickActionRenderer());
        doubleClickAction.addItem(DoubleClickAction.TRIGGER_BUILD);
        doubleClickAction.addItem(DoubleClickAction.LOAD_BUILDS);
        doubleClickAction.addItem(DoubleClickAction.SHOW_LAST_LOG);
    }

    public boolean isModified(JenkinsAppSettings jenkinsAppSettings) {
        boolean statusToIgnoreModified = successOrStableCheckBox.isSelected() != jenkinsAppSettings.shouldDisplaySuccessOrStable()
                || unstableOrFailCheckBox.isSelected() != jenkinsAppSettings.shouldDisplayFailOrUnstable()
                || abortedCheckBox.isSelected() != jenkinsAppSettings.shouldDisplayAborted();

        boolean isUseGreenColor = isUseGreenColor() != jenkinsAppSettings.isUseGreenColor();
        boolean isShowAllInStatusbar = isShowAllInStatusbar() != jenkinsAppSettings.isShowAllInStatusbar();
        boolean isAutoLoadBuilds = getAutoLoadBuilds() != jenkinsAppSettings.isAutoLoadBuilds();
        boolean isDoubleClickActionChanged = getDoubleClickAction() != jenkinsAppSettings.getDoubleClickAction();
        boolean isShowLogIfTriggerBuildChanged = isShowLogIfTriggerBuild() != jenkinsAppSettings.isShowLogIfTriggerBuild();

        return jenkinsAppSettings.getBuildDelay() != getBuildDelay()
                || jenkinsAppSettings.getJobRefreshPeriod() != getJobRefreshPeriod()
                || jenkinsAppSettings.getRssRefreshPeriod() != getRssRefreshPeriod()
                || jenkinsAppSettings.getNumBuildRetries() != getNumBuildRetries()
                || isUseGreenColor
                || isShowAllInStatusbar
                || isAutoLoadBuilds
                || isDoubleClickActionChanged
                || isShowLogIfTriggerBuildChanged
                || statusToIgnoreModified || (!jenkinsAppSettings.getSuffix().equals(replaceWithSuffix.getText()));
    }

    public void applyConfigurationData(JenkinsAppSettings jenkinsAppSettings) throws ConfigurationException {
        jenkinsAppSettings.setDelay(getBuildDelay());
        jenkinsAppSettings.setJobRefreshPeriod(getJobRefreshPeriod());
        jenkinsAppSettings.setRssRefreshPeriod(getRssRefreshPeriod());
        jenkinsAppSettings.setNumBuildRetries(getNumBuildRetries());

        jenkinsAppSettings.setIgnoreSuccessOrStable(successOrStableCheckBox.isSelected());
        jenkinsAppSettings.setDisplayUnstableOrFail(unstableOrFailCheckBox.isSelected());
        jenkinsAppSettings.setDisplayAborted(abortedCheckBox.isSelected());
        jenkinsAppSettings.setSuffix(getSuffix());
        jenkinsAppSettings.setUseGreenColor(isUseGreenColor());
        jenkinsAppSettings.setShowAllInStatusbar(isShowAllInStatusbar());
        jenkinsAppSettings.setAutoLoadBuilds(getAutoLoadBuilds());
        jenkinsAppSettings.setDoubleClickAction(getDoubleClickAction());
        jenkinsAppSettings.setShowLogIfTriggerBuild(isShowLogIfTriggerBuild());
    }

    private boolean isUseGreenColor() {
        return useGreenColor.isSelected();
    }

    private void setUseGreenColor(boolean useGreenColor) {
        this.useGreenColor.setSelected(useGreenColor);
    }

    private boolean getAutoLoadBuilds() {
        return autoLoadBuildsOnFirstLevel.isSelected();
    }

    private void setAutoLoadBuilds(boolean autoLoadBuilds) {
        this.autoLoadBuildsOnFirstLevel.setSelected(autoLoadBuilds);
    }

    @NotNull
    private DoubleClickAction getDoubleClickAction() {
        return Optional.ofNullable(doubleClickAction.getSelectedItem())
                .map(DoubleClickAction.class::cast)
                .orElse(DoubleClickAction.DEFAULT);
    }

    private boolean isShowAllInStatusbar() {
        return showAllInStatusbar.isSelected();
    }

    private void setShowAllInStatusbar(boolean useGreenColor) {
        this.showAllInStatusbar.setSelected(useGreenColor);
    }

    private boolean isShowLogIfTriggerBuild() {
        return showLogIfTriggerBuild.isSelected();
    }

    private void setShowLogIfTriggerBuild(boolean isShowLogIfTriggerBuild) {
        this.showLogIfTriggerBuild.setSelected(isShowLogIfTriggerBuild);
    }

    public void loadConfigurationData(JenkinsAppSettings jenkinsAppSettings) {
        buildDelay.setText(String.valueOf(jenkinsAppSettings.getBuildDelay()));

        jobRefreshPeriod.setText(String.valueOf(jenkinsAppSettings.getJobRefreshPeriod()));

        rssRefreshPeriod.setText(String.valueOf(jenkinsAppSettings.getRssRefreshPeriod()));
        numBuildRetries.setText(String.valueOf(jenkinsAppSettings.getNumBuildRetries()));

        successOrStableCheckBox.setSelected(jenkinsAppSettings.shouldDisplaySuccessOrStable());
        unstableOrFailCheckBox.setSelected(jenkinsAppSettings.shouldDisplayFailOrUnstable());
        abortedCheckBox.setSelected(jenkinsAppSettings.shouldDisplayAborted());

        replaceWithSuffix.setText(String.valueOf(jenkinsAppSettings.getSuffix()));
        setUseGreenColor(jenkinsAppSettings.isUseGreenColor());
        setShowAllInStatusbar(jenkinsAppSettings.isShowAllInStatusbar());
        setAutoLoadBuilds(jenkinsAppSettings.isAutoLoadBuilds());
        doubleClickAction.setSelectedItem(jenkinsAppSettings.getDoubleClickAction());
        setShowLogIfTriggerBuild(jenkinsAppSettings.isShowLogIfTriggerBuild());
    }

    private int getRssRefreshPeriod() {
        String period = rssRefreshPeriod.getText();
        if (StringUtils.isNotBlank(period)) {
            return Integer.parseInt(period);
        }
        return 0;
    }

    private int getNumBuildRetries() {
        String period = numBuildRetries.getText();
        if (StringUtils.isNotBlank(period)) {
            return Integer.parseInt(period);
        }
        return 1;
    }

    private int getJobRefreshPeriod() {
        String period = jobRefreshPeriod.getText();
        if (StringUtils.isNotBlank(period)) {
            return Integer.parseInt(period);
        }
        return 0;
    }

    private int getBuildDelay() {
        String delay = buildDelay.getText();
        if (StringUtils.isNotBlank(delay)) {
            return Integer.parseInt(delay);
        }
        return 0;
    }

    private String getSuffix() {
        return replaceWithSuffix.getText();
    }

}
