package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.model.ProjectJob;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderer;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers.NUMBER_OF_REQUIRING_INPUT_FILTER_LIST;

public class BuiltInJobParameterRenderer implements JobParameterRenderer {

    private final Map<JobParameterType, BiFunction<JobParameter, String, JobParameterComponent<?>>> converter = new HashMap<>();

    public BuiltInJobParameterRenderer() {
        converter.put(BuildInJobParameter.ChoiceParameterDefinition, JobParameterRenderers::createComboBoxOrInputFilterListIfChoicesExists);
        converter.put(BuildInJobParameter.BooleanParameterDefinition, JobParameterRenderers::createCheckBox);
        converter.put(BuildInJobParameter.StringParameterDefinition, JobParameterRenderers::createTextField);
        converter.put(BuildInJobParameter.PasswordParameterDefinition, JobParameterRenderers::createPasswordField);
        converter.put(BuildInJobParameter.TextParameterDefinition, JobParameterRenderers::createTextArea);
        converter.put(BuildInJobParameter.FileParameterDefinition, JobParameterRenderers::createFileUpload);
    }

    @NotNull
    @Override
    public JobParameterComponent<?> render(@NotNull JobParameter jobParameter, @Nullable ProjectJob projectJob) {
        final JobParameterType jobParameterType = jobParameter.getJobParameterType();
        final String defaultValue = jobParameter.getDefaultValue();
        return converter.getOrDefault(jobParameterType, JobParameterRenderers::createErrorLabel).apply(jobParameter, defaultValue);
    }

    @Override
    public boolean isForJobParameter(@NotNull JobParameter jobParameter) {
        return converter.containsKey(jobParameter.getJobParameterType());
    }

    @Nonnull
    @Override
    public Optional<JLabel> createLabel(@NotNull JobParameter jobParameter) {
        final JobParameterType jobParameterType = jobParameter.getJobParameterType();
        final Optional<JLabel> label = JobParameterRenderer.super.createLabel(jobParameter);
        boolean isTypeForInputTextFilter =
                BuildInJobParameter.ChoiceParameterDefinition.equals(jobParameterType);
        boolean isRequiringCountForInputTextFilter =
                jobParameter.getChoices().size() > NUMBER_OF_REQUIRING_INPUT_FILTER_LIST;
        if (isTypeForInputTextFilter && isRequiringCountForInputTextFilter) {
            label.ifPresent(textAreaLabel -> textAreaLabel.setVerticalAlignment(SwingConstants.TOP));
        }
        return label;
    }
}
