package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.model.ProjectJob;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.Optional;

/**
 * Render _class: jenkins.plugins.parameter_separator.ParameterSeparatorDefinition
 */
public class SeparatorRenderer implements JobParameterRenderer {

    @Nonnull
    @Override
    public Optional<JLabel> createLabel(@NotNull JobParameter jobParameter) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public JobParameterComponent<String> render(@NotNull JobParameter jobParameter, @Nullable ProjectJob projectJob) {
        return new JobParameterComponent<>(jobParameter, new JSeparator());
    }

    @Override
    public boolean isForJobParameter(@NotNull JobParameter jobParameter) {
        return Optional.of(jobParameter).map(JobParameter::getJobParameterType)
                .map(JobParameterType::getName)
                .filter("ParameterSeparatorDefinition"::equals).isPresent();
    }
}
