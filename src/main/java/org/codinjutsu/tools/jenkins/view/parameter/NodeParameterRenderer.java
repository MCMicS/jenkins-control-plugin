package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.model.ProjectJob;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderer;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class NodeParameterRenderer implements JobParameterRenderer {

    public static final JobParameterType NODE_PARAMETER = new JobParameterType("NodeParameterDefinition",
            "org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterDefinition");

    private final Map<JobParameterType, BiFunction<JobParameter, String, JobParameterComponent<String>>> converter = new HashMap<>();

    public NodeParameterRenderer() {
        converter.put(new JobParameterType("LabelParameterDefinition",
                        "org.jvnet.jenkins.plugins.nodelabelparameter.LabelParameterDefinition"),
                JobParameterRenderers::createTextField);
        converter.put(new JobParameterType("NodeParameterDefinition",
                        "org.jvnet.jenkins.plugins.nodelabelparameter.NodeParameterDefinition"),
                JobParameterRenderers::createComboBox);
    }

    @NotNull
    @Override
    public JobParameterComponent<String> render(@NotNull JobParameter jobParameter, @Nullable ProjectJob projectJob) {
        return converter.getOrDefault(jobParameter.getJobParameterType(), JobParameterRenderers::createErrorLabel)
                .apply(jobParameter, jobParameter.getDefaultValue());
    }

    @Override
    public boolean isForJobParameter(@NotNull JobParameter jobParameter) {
        return converter.containsKey(jobParameter.getJobParameterType());
    }
}
