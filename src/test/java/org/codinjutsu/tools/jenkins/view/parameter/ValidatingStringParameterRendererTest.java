package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class ValidatingStringParameterRendererTest implements JobParameterTest {

    private final ValidatingStringParameterRenderer jobParameterRenderer = new ValidatingStringParameterRenderer();

    @Test
    public void renderAsInputField() {
        JobParameter jobParameter = createJobParameter(ValidatingStringParameterRenderer.TYPE);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        final String defaultValue = "Default";
        jobParameter = createJobParameter(ValidatingStringParameterRenderer.TYPE, defaultValue);
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        final JTextField textField = (JTextField) jobParameterComponent.getViewElement();
        assertThat(textField.getText()).isEqualTo(defaultValue);
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ValidatingStringParameterRenderer.TYPE))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(GitParameterRenderer.PT_BRANCH))).isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.StringParameterDefinition)))
                .isFalse();
    }

}
