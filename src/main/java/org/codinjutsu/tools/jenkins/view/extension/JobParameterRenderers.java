package org.codinjutsu.tools.jenkins.view.extension;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.view.parameter.JobParameterComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.util.Arrays;
import java.util.function.Function;

@UtilityClass
public class JobParameterRenderers {

    public static final Icon ERROR_ICON = AllIcons.General.BalloonError;
    public static final String MISSING_NAME_LABEL = "<Missing Name>";

    @NotNull
    public static JobParameterComponent createPasswordField(JobParameter jobParameter, String defaultValue) {
        final JPasswordField passwordField = new JPasswordField();
        if (StringUtils.isNotEmpty(defaultValue)) {
            passwordField.setText(defaultValue);
        }
        return new JobParameterComponent(jobParameter, passwordField, readPassword());
    }

    @NotNull
    public static JobParameterComponent createTextArea(JobParameter jobParameter, String defaultValue) {
        final JTextArea textArea = new JTextArea();
        textArea.setRows(5);
        if (StringUtils.isNotEmpty(defaultValue)) {
            textArea.setText(defaultValue);
        }
        return new JobParameterComponent(jobParameter, textArea, JTextComponent::getText);
    }

    @NotNull
    public static JobParameterComponent createTextField(JobParameter jobParameter, String defaultValue) {
        final JTextField textField = new JTextField();
        if (StringUtils.isNotEmpty(defaultValue)) {
            textField.setText(defaultValue);
        }
        return new JobParameterComponent(jobParameter, textField, JTextComponent::getText);
    }

    @NotNull
    public static JobParameterComponent createCheckBox(JobParameter jobParameter, String defaultValue) {
        final JCheckBox checkBox = new JCheckBox();
        if (Boolean.TRUE.equals(Boolean.valueOf(defaultValue))) {
            checkBox.setSelected(true);
        }
        return new JobParameterComponent(jobParameter, checkBox, asString(JCheckBox::isSelected));
    }

    @NotNull
    public static JobParameterComponent createComboBox(@NotNull JobParameter jobParameter, String defaultValue) {
        final String[] choices = jobParameter.getChoices().toArray(new String[0]);
        ComboBox<String> comboBox = new ComboBox<>(choices);
        if (StringUtils.isNotEmpty(defaultValue)) {
            comboBox.setSelectedItem(defaultValue);
        }
        return new JobParameterComponent(jobParameter, comboBox, asString(JComboBox::getSelectedItem));
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
    public static JobParameterComponent createErrorLabel(@NotNull JobParameter jobParameter) {
        return new JobParameterComponent(jobParameter, createErrorLabel(jobParameter.getJobParameterType()), () -> true);
    }

    @NotNull
    public static JobParameterComponent createErrorLabel(@NotNull JobParameter jobParameter, String defaultValue) {
        return createErrorLabel(jobParameter);
    }

    @NotNull
    private static Function<JPasswordField, String> readPassword() {
        return passwordField -> {
            final char[] password = passwordField.getPassword();
            final String result = new String(password);
            Arrays.fill(password, '\0');
            return result;
        };
    }

    @NotNull
    private static <T> Function<T, String> asString(Function<T, Object> provider) {
        return c -> String.valueOf(provider.apply(c));
    }

    public class ErrorLabel extends JLabel {

        public ErrorLabel(@Nullable String text) {
            setText(text);
            setIcon(ERROR_ICON);
        }
    }
}
