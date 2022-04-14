package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class ActiveChoicesParameterRendererTest implements JobParameterTest {

    private final ActiveChoicesParameterRenderer jobParameterRenderer = new ActiveChoicesParameterRenderer();

    @Test
    public void renderAsInputField() {
        JobParameter jobParameter = createJobParameter(ActiveChoicesParameterRenderer.CHOICE_PARAMETER);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(ActiveChoicesParameterRenderer.CASCADE_CHOICE_PARAMETER);
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);
    }

    @Test
    public void renderAsLabel() {
        JobParameter jobParameter = createJobParameter(ActiveChoicesParameterRenderer.DYNAMIC_REFERENCE_PARAMETER);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ActiveChoicesParameterRenderer.CHOICE_PARAMETER))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ActiveChoicesParameterRenderer.CASCADE_CHOICE_PARAMETER))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ActiveChoicesParameterRenderer.DYNAMIC_REFERENCE_PARAMETER))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition))).isFalse();
    }

}
