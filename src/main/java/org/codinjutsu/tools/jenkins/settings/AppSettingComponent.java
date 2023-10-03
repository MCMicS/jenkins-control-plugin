package org.codinjutsu.tools.jenkins.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBDimension;
import com.intellij.util.ui.JBUI;
import org.codinjutsu.tools.jenkins.DoubleClickAction;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsControlBundle;
import org.codinjutsu.tools.jenkins.view.DoubleClickActionRenderer;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidationPanel;
import org.codinjutsu.tools.jenkins.view.annotation.GuiField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static org.codinjutsu.tools.jenkins.util.GuiUtil.createLabeledComponent;
import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.POSITIVE_INTEGER;

public class AppSettingComponent implements FormValidationPanel {
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner buildDelay = new JBIntSpinner(0, 0, 3000);
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner jobRefreshPeriod = new JBIntSpinner(0, 0, 120);
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner rssRefreshPeriod = new JBIntSpinner(0, 0, 300);
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner numBuildRetries = new JBIntSpinner(0, 0, 50);
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner buildsToLoadPerJob = new JBIntSpinner(0, 0, 100);
    private final ComboBox<DoubleClickAction> doubleClickAction = createDoubleClickActionComboBox();

    private final JBCheckBox useGreenColor = new JBCheckBox(JenkinsControlBundle.message("settings.app.useGreenColor"));
    private final JBCheckBox showAllInStatusbar = new JBCheckBox(JenkinsControlBundle.message("settings.app.statusBar.showAllBuilds"));
    private final JBCheckBox autoLoadBuildsOnFirstLevel = new JBCheckBox(JenkinsControlBundle.message("settings.app.autoLoadBuilds"));
    private final JBCheckBox showLogIfTriggerBuild = new JBCheckBox(JenkinsControlBundle.message("settings.app.showLogOnTrigger"));
    private final JBCheckBox successOrStableCheckBox = new JBCheckBox(JenkinsControlBundle.message("settings.app.rss.successOrStable"));
    private final JBCheckBox unstableOrFailCheckBox = new JBCheckBox(JenkinsControlBundle.message("settings.app.rss.unstableOrFail"));
    private final JBCheckBox abortedCheckBox = new JBCheckBox(JenkinsControlBundle.message("settings.app.rss.aborted"));
    private final JBTextField replaceWithSuffix = new JBTextField();

    private final JPanel mainPanel;

    public AppSettingComponent() {

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.buildDelay.label"),
                        createLabeledComponent(buildDelay, JenkinsControlBundle.message("settings.seconds")))
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.jobRefresh.label"),
                        createLabeledComponent(jobRefreshPeriod, JenkinsControlBundle.message("settings.minutes")))
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.rssRefresh.label"),
                        createLabeledComponent(rssRefreshPeriod, JenkinsControlBundle.message("settings.minutes")))
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.numBuildRetries.label"),
                        numBuildRetries)
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.buildsToLoad.label"),
                        buildsToLoadPerJob)
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.doubleClickAction.label"),
                        doubleClickAction)
                .addComponent(useGreenColor)
                .addComponent(showAllInStatusbar)
                .addComponent(autoLoadBuildsOnFirstLevel)
                .addComponent(showLogIfTriggerBuild)
                .addComponent(createRssSettingPanel())
                .addComponent(createUploadPatchPanel())
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private static ComboBox<DoubleClickAction> createDoubleClickActionComboBox() {
        final var doubleClickAction = new ComboBox<DoubleClickAction>();
        final JBDimension size = JBUI.size(245, doubleClickAction.getPreferredSize().height);
        doubleClickAction.setPreferredSize(size);
        doubleClickAction.setEditable(false);
        doubleClickAction.setRenderer(new DoubleClickActionRenderer());
        doubleClickAction.addItem(DoubleClickAction.TRIGGER_BUILD);
        doubleClickAction.addItem(DoubleClickAction.LOAD_BUILDS);
        doubleClickAction.addItem(DoubleClickAction.SHOW_LAST_LOG);
        return doubleClickAction;
    }

    public @NotNull JPanel getPanel() {
        return mainPanel;
    }

    private @NotNull JPanel createRssSettingPanel() {
        final var panel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT));
        panel.add(successOrStableCheckBox);
        panel.add(unstableOrFailCheckBox);
        panel.add(abortedCheckBox);
        final var rssSettingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.rss.statusFilter.label"), panel)
                .getPanel();
        rssSettingsPanel.setBorder(IdeBorderFactory.createTitledBorder(//
                JenkinsControlBundle.message("settings.app.rss.statusFilter"), true));
        return rssSettingsPanel;
    }

    private @NotNull JPanel createUploadPatchPanel() {
        final var uploadPatchSettingsPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.uploadPatch.label"), replaceWithSuffix)
                .addComponentToRightColumn(new JBLabel(JenkinsControlBundle.message("settings.app.uploadPatch.macros")))
                .getPanel();
        uploadPatchSettingsPanel.setBorder(IdeBorderFactory.createTitledBorder(
                JenkinsControlBundle.message("settings.app.uploadPatch"), true));
        return uploadPatchSettingsPanel;
    }

    public JenkinsAppSettings getSetting() {
        final JenkinsAppSettings jenkinsAppSettings = new JenkinsAppSettings();
        jenkinsAppSettings.setDelay(buildDelay.getNumber());
        jenkinsAppSettings.setJobRefreshPeriod(jobRefreshPeriod.getNumber());
        jenkinsAppSettings.setRssRefreshPeriod(rssRefreshPeriod.getNumber());
        jenkinsAppSettings.setNumBuildRetries(numBuildRetries.getNumber());
        jenkinsAppSettings.setBuildsToLoadPerJob(buildsToLoadPerJob.getNumber());

        jenkinsAppSettings.setDisplaySuccessOrStable(successOrStableCheckBox.isSelected());
        jenkinsAppSettings.setDisplayUnstableOrFail(unstableOrFailCheckBox.isSelected());
        jenkinsAppSettings.setDisplayAborted(abortedCheckBox.isSelected());
        jenkinsAppSettings.setSuffix(replaceWithSuffix.getText());
        jenkinsAppSettings.setUseGreenColor(useGreenColor.isSelected());
        jenkinsAppSettings.setShowAllInStatusbar(showAllInStatusbar.isSelected());
        jenkinsAppSettings.setAutoLoadBuilds(autoLoadBuildsOnFirstLevel.isSelected());
        jenkinsAppSettings.setDoubleClickAction(doubleClickAction.getItem());
        jenkinsAppSettings.setShowLogIfTriggerBuild(showLogIfTriggerBuild.isSelected());
        return jenkinsAppSettings;
    }

    public void setBuildDelay(int buildDelay) {
        this.buildDelay.setNumber(buildDelay);
    }

    public void setJobRefreshPeriod(int jobRefreshPeriod) {
        this.jobRefreshPeriod.setNumber(jobRefreshPeriod);
    }

    public void setRssRefreshPeriod(int rssRefreshPeriod) {
        this.rssRefreshPeriod.setNumber(rssRefreshPeriod);
    }

    public void setNumBuildRetries(int numBuildRetries) {
        this.numBuildRetries.setNumber(numBuildRetries);
    }

    public void setBuildsToLoadPerJob(int buildsToLoadPerJob) {
        this.buildsToLoadPerJob.setNumber(buildsToLoadPerJob);
    }

    public void setShouldDisplaySuccessOrStable(boolean displaySuccessOrStable) {
        this.successOrStableCheckBox.setSelected(displaySuccessOrStable);
    }

    public void setShouldDisplayUnstableOrFail(boolean displayUnstableOrFail) {
        this.unstableOrFailCheckBox.setSelected(displayUnstableOrFail);

    }

    public void setShouldDisplayAborted(boolean displayAborted) {
        this.abortedCheckBox.setSelected(displayAborted);
    }

    public void setReplaceWithSuffix(String suffix) {
        this.replaceWithSuffix.setText(suffix);
    }

    public void setDoubleClickAction(DoubleClickAction doubleClickAction) {
        this.doubleClickAction.setItem(doubleClickAction);
    }

    public void setUseGreenColor(boolean useGreenColor) {
        this.useGreenColor.setSelected(useGreenColor);
    }

    public void setShowAllInStatusbar(boolean showAllInStatusbar) {
        this.showAllInStatusbar.setSelected(showAllInStatusbar);
    }

    public void setAutoLoadBuilds(boolean autoLoadBuilds) {
        this.autoLoadBuildsOnFirstLevel.setSelected(autoLoadBuilds);
    }

    public void setShowLogIfTriggerBuild(boolean showLogIfTriggerBuild) {
        this.showLogIfTriggerBuild.setSelected(showLogIfTriggerBuild);
    }
}
