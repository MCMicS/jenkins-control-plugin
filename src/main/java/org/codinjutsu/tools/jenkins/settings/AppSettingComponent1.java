package org.codinjutsu.tools.jenkins.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.codinjutsu.tools.jenkins.DoubleClickAction;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsControlBundle;
import org.codinjutsu.tools.jenkins.view.annotation.FormValidationPanel;
import org.codinjutsu.tools.jenkins.view.annotation.GuiField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static org.codinjutsu.tools.jenkins.util.GuiUtil.createLabeledComponent;
import static org.codinjutsu.tools.jenkins.util.GuiUtil.simplePanel;
import static org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum.POSITIVE_INTEGER;

public class AppSettingComponent1 implements FormValidationPanel {
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner buildDelay = new JBIntSpinner(0, 0, 3000);
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner jobRefreshPeriod = new JBIntSpinner(0, 5, 120);
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner rssRefreshPeriod = new JBIntSpinner(0, 5, 300);
    @GuiField(validators = POSITIVE_INTEGER)
    private final JBIntSpinner numBuildRetries = new JBIntSpinner(0, 0, 50);
    private final ComboBox<DoubleClickAction> doubleClickAction = new ComboBox<>();
    private final JBCheckBox useGreenColor = new JBCheckBox(JenkinsControlBundle.message("settings.app.useGreenColor"));
    private final JBCheckBox showAllInStatusbar = new JBCheckBox(JenkinsControlBundle.message("settings.app.statusBar.showAllBuilds"));
    private final JBCheckBox autoLoadBuildsOnFirstLevel = new JBCheckBox(JenkinsControlBundle.message("settings.app.autoLoadBuilds"));
    private final JBCheckBox showLogIfTriggerBuild = new JBCheckBox(JenkinsControlBundle.message("settings.app.showLogOnTrigger"));
    private final JBCheckBox successOrStableCheckBox = new JBCheckBox(JenkinsControlBundle.message("settings.app.rss.successOrStable"));
    private final JBCheckBox unstableOrFailCheckBox = new JBCheckBox(JenkinsControlBundle.message("settings.app.rss.unstableOrFail"));
    private final JBCheckBox abortedCheckBox = new JBCheckBox(JenkinsControlBundle.message("settings.app.rss.aborted"));
    private final JBTextField replaceWithSuffix = new JBTextField();

    private final JPanel mainPanel;

    public AppSettingComponent1() {
        final var rssSettingsPanel = new JPanel(new BorderLayout());
        rssSettingsPanel.setBorder(IdeBorderFactory.createTitledBorder(//
                JenkinsControlBundle.message("settings.app.rss.statusFilter"), true));
        final var uploadPatchSettingsPanel = new JPanel(new BorderLayout());
        uploadPatchSettingsPanel.setBorder(IdeBorderFactory.createTitledBorder(
                JenkinsControlBundle.message("settings.app.uploadPatch"), true));

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.buildDelay.label"),
                        createLabeledComponent(buildDelay, JenkinsControlBundle.message("settings.seconds")))
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.jobRefresh.label"),
                        createLabeledComponent(jobRefreshPeriod, JenkinsControlBundle.message("settings.minutes")))
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.rssRefresh.label"),
                        createLabeledComponent(rssRefreshPeriod, JenkinsControlBundle.message("settings.minutes")))
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.numBuildRetries.label"),
                        numBuildRetries)
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.doubleClickAction.label"),
                        doubleClickAction)
                .addComponent(useGreenColor)
                .addComponent(showAllInStatusbar)
                .addComponent(autoLoadBuildsOnFirstLevel)
                .addComponent(showLogIfTriggerBuild)
                .addComponent(createRssSettingPanel())
                .addComponent(uploadPatchSettingsPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public @NotNull JPanel getPanel() {
        return mainPanel;
    }

    private @NotNull JPanel createRssSettingPanel() {
        final var panel = simplePanel();
        panel.addToCenter(successOrStableCheckBox);
        panel.addToCenter(unstableOrFailCheckBox);
        panel.addToCenter(abortedCheckBox);
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.rss.statusFilter.label"), panel)
                .getPanel();
    }

    private @NotNull JPanel createUploadPatchPanel() {
        final var panel = simplePanel();
        panel.addToCenter(successOrStableCheckBox);
        panel.addToCenter(unstableOrFailCheckBox);
        panel.addToCenter(abortedCheckBox);
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(JenkinsControlBundle.message("settings.app.uploadPatch.label"), replaceWithSuffix)
                .addComponentToRightColumn(new JBLabel(JenkinsControlBundle.message("settings.app.uploadPatch.macros")))
                .getPanel();
    }

    public JenkinsAppSettings getSetting() {
        return new JenkinsAppSettings();
    }
}
