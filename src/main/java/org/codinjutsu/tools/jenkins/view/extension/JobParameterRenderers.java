package org.codinjutsu.tools.jenkins.view.extension;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
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
        if (StringUtils.isNotEmpty(defaultValue)) {
            textFieldWithBrowseButton.setText(defaultValue);
        }
        return new JobParameterComponent<>(jobParameter, textFieldWithBrowseButton, JobParameterRenderers.getFile());
    }

    @NotNull
    public static JobParameterComponent<String> createPasswordField(JobParameter jobParameter, String defaultValue) {
        final PasswordComponent passwordComponent = PasswordComponent.create();
        passwordComponent.init();
        if (StringUtils.isNotEmpty(defaultValue)) {
            passwordComponent.setValue(defaultValue);
        }
        return new JobParameterComponent<>(jobParameter, passwordComponent.asComponent(), c -> passwordComponent.getValue());
    }

    @NotNull
    public static JobParameterComponent<String> createTextArea(JobParameter jobParameter, String defaultValue) {
        final JTextArea textArea = new JBTextArea();
        textArea.setRows(5);
        if (StringUtils.isNotEmpty(defaultValue)) {
            textArea.setText(defaultValue);
        }
        return new JobParameterComponent<>(jobParameter, textArea, JTextComponent::getText);
    }

    @NotNull
    public static JobParameterComponent<String> createTextField(JobParameter jobParameter, String defaultValue) {
        final JTextField textField = new JBTextField();
        if (StringUtils.isNotEmpty(defaultValue)) {
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
        if (StringUtils.isNotEmpty(defaultValue)) {
            comboBox.setSelectedItem(defaultValue);
        }
        return new JobParameterComponent<>(jobParameter, comboBox, asString(JComboBox::getSelectedItem));
    }

    @NotNull
    public static JLabel createErrorLabel(@Nullable JobParameterType jobParameterType) {
        final String text;
        if (jobParameterType == null) {
            text = "Unknown parameter type";
        } else {
            text = jobParameterType.getName() + " is unsupported.";
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
    private static <T> Function<T, String> asString(Function<T, Object> provider) {
        return c -> String.valueOf(provider.apply(c));
    }

    @NotNull
    public static Function<TextFieldWithBrowseButton, VirtualFile> getFile() {
        return JobParameterRenderers::getFile;
    }

    @Nullable
    private static VirtualFile getFile(@NotNull TextFieldWithBrowseButton filePathField) {
        if (!StringUtils.isEmpty(filePathField.getText())) {
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
