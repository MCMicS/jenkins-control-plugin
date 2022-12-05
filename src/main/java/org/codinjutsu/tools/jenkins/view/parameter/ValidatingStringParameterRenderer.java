package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.model.ProjectJob;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderer;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ValidatingStringParameterRenderer implements JobParameterRenderer {

    @NonNls
    private static final String TYPE_CLASS = "hudson.plugins.validating_string_parameter.ValidatingStringParameterDefinition";

    static final JobParameterType TYPE = new JobParameterType("ValidatingStringParameterDefinition", TYPE_CLASS);

    @Override
    public @NotNull JobParameterComponent render(@NotNull JobParameter jobParameter, @Nullable ProjectJob projectJob) {
        return JobParameterRenderers.createTextField(jobParameter, jobParameter.getDefaultValue());
    }

    @Override
    public boolean isForJobParameter(@NotNull JobParameter jobParameter) {
        return TYPE.equals(jobParameter.getJobParameterType());
    }
}
