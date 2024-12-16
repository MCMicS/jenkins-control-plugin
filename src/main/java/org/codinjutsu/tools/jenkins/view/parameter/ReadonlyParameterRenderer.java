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

public class ReadonlyParameterRenderer implements JobParameterRenderer {

    static final JobParameterType STRING = new JobParameterType("WReadonlyStringParameterDefinition",
            "com.wangyin.ams.cms.abs.ParaReadOnly.WReadonlyStringParameterDefinition");

    static final JobParameterType TEXT = new JobParameterType("WReadonlyTextParameterDefinition",
            "com.wangyin.ams.cms.abs.ParaReadOnly.WReadonlyTextParameterDefinition");

    private final Map<JobParameterType, BiFunction<JobParameter, String, JobParameterComponent<?>>> converter = new HashMap<>();

    public ReadonlyParameterRenderer() {
        converter.put(STRING, JobParameterRenderers::createReadonlyTextField);
        converter.put(TEXT, JobParameterRenderers::createReadonlyTextArea);
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
}
