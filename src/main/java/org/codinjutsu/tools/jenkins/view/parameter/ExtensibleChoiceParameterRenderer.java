package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderer;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class ExtensibleChoiceParameterRenderer implements JobParameterRenderer {

    @NonNls
    private static final String TYPE_CLASS = "jp.ikedam.jenkins.plugins.extensible_choice_parameter.ExtensibleChoiceParameterDefinition";

    static final JobParameterType TYPE = new JobParameterType("ExtensibleChoiceParameterDefinition", TYPE_CLASS);

    private final Map<JobParameterType, BiFunction<JobParameter, String, JobParameterComponent<String>>> converter =
            new HashMap<>();

    public ExtensibleChoiceParameterRenderer() {
        converter.put(TYPE, JobParameterRenderers::createComboBoxIfChoicesExists);
    }

    @NotNull
    @Override
    public JobParameterComponent<String> render(@NotNull JobParameter jobParameter) {
        return converter.getOrDefault(jobParameter.getJobParameterType(), JobParameterRenderers::createErrorLabel)
                .apply(jobParameter, jobParameter.getDefaultValue());
    }

    @Override
    public boolean isForJobParameter(@NotNull JobParameter jobParameter) {
        return converter.containsKey(jobParameter.getJobParameterType());
    }
}
