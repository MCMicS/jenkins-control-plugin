package org.codinjutsu.tools.jenkins.view.extension;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import lombok.experimental.UtilityClass;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.logic.RequestManagerInterface;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.model.ProjectJob;
import org.codinjutsu.tools.jenkins.view.parameter.JobParameterComponent;
import org.codinjutsu.tools.jenkins.view.parameter.PasswordComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.function.BiFunction;
import java.util.function.Function;

@UtilityClass
public final class JobParameterRenderers {

    public static final Icon ERROR_ICON = AllIcons.General.BalloonError;
    public static final String MISSING_NAME_LABEL = "<Missing Name>";

    @NotNull
    public static JobParameterComponent<VirtualFile> createFileUpload(JobParameter jobParameter, String defaultValue) {
        final TextFieldWithBrowseButton textFieldWithBrowseButton = new TextFieldWithBrowseButton();
        final Project project = null;
        textFieldWithBrowseButton.addBrowseFolderListener(jobParameter.getName(), jobParameter.getDescription(), project,
                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
        textFieldWithBrowseButton.setTextFieldPreferredWidth(30);
        if (StringUtil.isNotEmpty(defaultValue)) {
            textFieldWithBrowseButton.setText(defaultValue);
        }
        return new JobParameterComponent<>(jobParameter, textFieldWithBrowseButton, JobParameterRenderers.getFile());
    }

    @NotNull
    public static JobParameterComponent<String> createPasswordField(JobParameter jobParameter, String defaultValue) {
        final PasswordComponent passwordComponent = PasswordComponent.create();
        passwordComponent.init();
        if (StringUtil.isNotEmpty(defaultValue)) {
            passwordComponent.setValue(defaultValue);
        }
        return new JobParameterComponent<>(jobParameter, passwordComponent.asComponent(), c -> passwordComponent.getValue());
    }

    @NotNull
    public static JobParameterComponent<String> createTextArea(JobParameter jobParameter, String defaultValue) {
        final JTextArea textArea = new JBTextArea();
        textArea.setRows(5);
        if (StringUtil.isNotEmpty(defaultValue)) {
            textArea.setText(defaultValue);
        }
        return new JobParameterComponent<>(jobParameter, textArea, JTextComponent::getText);
    }

    @NotNull
    public static JobParameterComponent<String> createTextField(JobParameter jobParameter, String defaultValue) {
        final JTextField textField = new JBTextField();
        if (StringUtil.isNotEmpty(defaultValue)) {
            textField.setText(defaultValue);
        }
        return new JobParameterComponent<>(jobParameter, textField, JTextComponent::getText);
    }

    @NotNull
    public static JobParameterComponent<String> createCheckBox(JobParameter jobParameter, String defaultValue) {
        final JCheckBox checkBox = new JCheckBox();
        if (Boolean.TRUE.equals(Boolean.valueOf(defaultValue))) {
            checkBox.setSelected(true);
        }
        return new JobParameterComponent<>(jobParameter, checkBox, asString(JCheckBox::isSelected));
    }

    @NotNull
    public static JobParameterComponent<String> createComboBox(@NotNull JobParameter jobParameter, String defaultValue) {
        final String[] choices = jobParameter.getChoices().toArray(new String[0]);
        ComboBox<String> comboBox = new ComboBox<>(choices);
        if (StringUtil.isNotEmpty(defaultValue)) {
            comboBox.setSelectedItem(defaultValue);
        }
        return new JobParameterComponent<>(jobParameter, comboBox, asString(JComboBox::getSelectedItem));
    }

    @SuppressWarnings("unused")
    @NotNull
    public static JobParameterComponent<String> createLabel(@NotNull JobParameter jobParameter, String defaultValue) {
        final JBLabel label = new JBLabel();
        if (StringUtil.isNotEmpty(defaultValue)) {
            label.setText(defaultValue);
        }
        return new JobParameterComponent<>(jobParameter, label, JLabel::getText);
    }

    @NotNull
    public static JLabel createErrorLabel(@Nullable JobParameterType jobParameterType) {
        final String text;
        if (jobParameterType == null) {
            text = "Unknown parameter type";
        } else {
            text = jobParameterType.getType() + " is unsupported.";
        }
        return createErrorLabel(text);
    }

    @NotNull
    public static JLabel createErrorLabel(@NotNull String label) {
        return new ErrorLabel(label);
    }

    @NotNull
    public static JobParameterComponent<String> createErrorLabel(@NotNull JobParameter jobParameter) {
        return new JobParameterComponent<>(jobParameter, createErrorLabel(jobParameter.getJobParameterType()), () -> true);
    }

    @SuppressWarnings("unused")
    @NotNull
    public static JobParameterComponent<String> createErrorLabel(@NotNull JobParameter jobParameter, String defaultValue) {
        return createErrorLabel(jobParameter);
    }

    @NotNull
    public static JobParameterComponent<String> createComboBoxIfChoicesExists(@NotNull JobParameter jobParameter,
                                                                              String defaultValue) {
        final BiFunction<JobParameter, String, JobParameterComponent<String>> renderer;
        if (jobParameter.getChoices().isEmpty()) {
            renderer = JobParameterRenderers::createTextField;
        } else {
            renderer = JobParameterRenderers::createComboBox;
        }
        return renderer.apply(jobParameter, defaultValue);
    }

    @NotNull
    public static Function<JobParameter, JobParameterComponent<String>> createGitParameterChoices(
            @NotNull ProjectJob projectJob) {
        return jobParameter -> createGitParameterChoices(projectJob, jobParameter, jobParameter.getDefaultValue());
    }

    @NotNull
    public static JobParameterComponent<String> createGitParameterChoices(@NotNull ProjectJob projectJob,
                                                                         @NotNull JobParameter jobParameter,
                                                                         String defaultValue) {
        if (jobParameter.getChoices().isEmpty()) {
            final RequestManagerInterface requestManager = RequestManager.getInstance(projectJob.getProject());
            JobParameter gitParameter = JobParameter.builder()
                    .name(jobParameter.getName())
                    .description(jobParameter.getDescription())
                    .jobParameterType(jobParameter.getJobParameterType())
                    .defaultValue(jobParameter.getDefaultValue())
                    .choices(requestManager.getGitParameterChoices(projectJob.getJob(), jobParameter))
                    .build();
            return createComboBoxIfChoicesExists(gitParameter, defaultValue);
        } else {
            return createComboBoxIfChoicesExists(jobParameter, defaultValue);
        }
    }

    @NotNull
    private static <T> Function<T, String> asString(Function<T, Object> provider) {
        return c -> String.valueOf(provider.apply(c));
    }

    @NotNull
    public static Function<TextFieldWithBrowseButton, VirtualFile> getFile() {
        return JobParameterRenderers::getFile;
    }

    @Nullable
    private static VirtualFile getFile(@NotNull TextFieldWithBrowseButton filePathField) {
        if (!StringUtil.isEmpty(filePathField.getText())) {
            // use com.intellij.openapi.vfs.VirtualFileLookup (2020.2 and later)
            final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePathField.getText());
            if (file != null && !file.isDirectory()) {
                return file;
            }
        }
        return null;
    }

    public static class ErrorLabel extends JLabel {

        public ErrorLabel(@Nullable String text) {
            setText(text);
            setIcon(ERROR_ICON);
        }
    }
}
