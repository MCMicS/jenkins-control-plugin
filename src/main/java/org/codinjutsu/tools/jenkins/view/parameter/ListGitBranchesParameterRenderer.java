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

public class ListGitBranchesParameterRenderer implements JobParameterRenderer {

    @NonNls
    private static final String TYPE_CLASS = "com.syhuang.hudson.plugins.listgitbranchesparameter.ListGitBranchesParameterDefinition";

    static final JobParameterType PT_TAG = new JobParameterType("PT_TAG", TYPE_CLASS);

    static final JobParameterType PT_BRANCH = new JobParameterType("PT_BRANCH", TYPE_CLASS);

    static final JobParameterType PT_BRANCH_TAG = new JobParameterType("PT_BRANCH_TAG", TYPE_CLASS);

    private final Map<JobParameterType, BiFunction<JobParameter, String, JobParameterComponent<String>>> converter = new HashMap<>();

    public ListGitBranchesParameterRenderer() {
        converter.put(PT_TAG, GitParameterRenderer::createComponent);
        converter.put(PT_BRANCH, GitParameterRenderer::createComponent);
        converter.put(PT_BRANCH_TAG, GitParameterRenderer::createComponent);
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
