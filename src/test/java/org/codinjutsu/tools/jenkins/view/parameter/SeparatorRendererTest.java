package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class SeparatorRendererTest implements JobParameterTest {

    private final SeparatorRenderer jobParameterRenderer = new SeparatorRenderer();
    private final JobParameterType parameterSeparatorDefinition = new JobParameterType("ParameterSeparatorDefinition",
            "jenkins.plugins.parameter_separator.ParameterSeparatorDefinition");

    @Test
    public void render() {
        final JobParameter jobParameter = createJobParameter(parameterSeparatorDefinition);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JSeparator.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JSeparator.class);
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(parameterSeparatorDefinition)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(new JobParameterType("ParameterSeparatorDefinition",
                "jenkins.plugins.other_parameter_separator.ParameterSeparator2Definition"))))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition)))
                .isFalse();
    }
}
