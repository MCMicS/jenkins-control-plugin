package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.model.ProjectJob;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderer;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GitParameterRenderer implements JobParameterRenderer {

    @NonNls
    private static final String TYPE_CLASS = "net.uaznia.lukanus.hudson.plugins.gitparameter.GitParameterDefinition";

    static final JobParameterType PT_TAG = new JobParameterType("PT_TAG", TYPE_CLASS);

    static final JobParameterType PT_BRANCH = new JobParameterType("PT_BRANCH", TYPE_CLASS);

    static final JobParameterType PT_BRANCH_TAG = new JobParameterType("PT_BRANCH_TAG", TYPE_CLASS);

    static final JobParameterType PT_REVISION = new JobParameterType("PT_REVISION", TYPE_CLASS);

    static final JobParameterType PT_PULL_REQUEST = new JobParameterType("PT_PULL_REQUEST", TYPE_CLASS);

    static final JobParameterType GIT_PARAMETER_DEFINITION = new JobParameterType("GitParameterDefinition", TYPE_CLASS);

    private final Set<JobParameterType> validTypes = new HashSet<>();

    public GitParameterRenderer() {
        validTypes.add(PT_TAG);
        validTypes.add(PT_BRANCH);
        validTypes.add(PT_BRANCH_TAG);
        validTypes.add(PT_REVISION);
        validTypes.add(PT_PULL_REQUEST);
        validTypes.add(GIT_PARAMETER_DEFINITION);
    }

    public GitParameterRenderer(Set<JobParameterType> validTypes) {
        this.validTypes.addAll(validTypes);
    }

    @NotNull
    @Override
    public JobParameterComponent<String> render(@NotNull JobParameter jobParameter, @Nullable ProjectJob projectJob) {
        if (!validTypes.contains(jobParameter.getJobParameterType())) {
            return JobParameterRenderers.createErrorLabel(jobParameter);
        }
        if (projectJob == null) {
            return JobParameterRenderers.createInputFilterListIfChoicesExists(jobParameter, jobParameter.getDefaultValue());
        } else {
            return JobParameterRenderers.createGitParameterChoices(projectJob).apply(jobParameter);
        }
    }

    @Nonnull
    @Override
    public Optional<JLabel> createLabel(@NotNull JobParameter jobParameter) {
        final Optional<JLabel> label = JobParameterRenderer.super.createLabel(jobParameter);
        label.ifPresent(textAreaLabel -> textAreaLabel.setVerticalAlignment(SwingConstants.TOP));
        return label;
    }

    @Override
    public boolean isForJobParameter(@NotNull JobParameter jobParameter) {
        return validTypes.contains(jobParameter.getJobParameterType());
    }
}
