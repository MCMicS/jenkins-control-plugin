package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HiddenParameterRenderer implements JobParameterRenderer {

    static final JobParameterType TYPE = new JobParameterType("WHideParameterDefinition",
            "com.wangyin.parameter.WHideParameterDefinition");

    @NotNull
    @Override
    public JobParameterComponent render(@NotNull JobParameter jobParameter) {
        return new JobParameterComponent(jobParameter, new JLabel(), false);
    }

    @Override
    public boolean isForJobParameter(@NotNull JobParameter jobParameter) {
        return TYPE.equals(jobParameter.getJobParameterType());
    }
}
