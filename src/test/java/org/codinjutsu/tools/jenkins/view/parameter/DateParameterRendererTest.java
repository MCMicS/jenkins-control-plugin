package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class DateParameterRendererTest implements JobParameterTest {

    private final DateParameterRenderer jobParameterRenderer = new DateParameterRenderer();

    @Test
    public void renderAsInputField() {
        JobParameter jobParameter = createJobParameter(DateParameterRenderer.DATE_PARAMETER);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);
    }

    @Test
    public void renderWithDefaultValue() {
        final String defaultValue = "2021-05-15";
        JobParameter jobParameter = createJobParameter(DateParameterRenderer.DATE_PARAMETER, defaultValue);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        final JTextField textField = (JTextField) jobParameterComponent.getViewElement();
        assertThat(textField.getText()).isEqualTo(defaultValue);
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(DateParameterRenderer.DATE_PARAMETER)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(
                BuildInJobParameter.ChoiceParameterDefinition))).isFalse();
    }

}
