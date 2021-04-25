package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.openapi.ui.ComboBox;
import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class ExtendedChoiceParameterRendererTest implements JobParameterTest {

    private final ExtendedChoiceParameterRenderer jobParameterRenderer = new ExtendedChoiceParameterRenderer();

    @Test
    public void renderAsInputField() {
        JobParameter jobParameter = createJobParameter(ExtendedChoiceParameterRenderer.PT_SINGLE_SELECT);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(ExtendedChoiceParameterRenderer.PT_MULTI_SELECT);
        jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(ExtendedChoiceParameterRenderer.PT_TEXTBOX);
        jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);
    }

    @Test
    public void renderAsComboboxIfApiValuesAreAvailable() {
        JobParameter jobParameter = createJobParameter(ExtendedChoiceParameterRenderer.PT_SINGLE_SELECT,
                "Value1", "Selected", "Value3");
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(ExtendedChoiceParameterRenderer.PT_MULTI_SELECT,
                "Value1", "Selected", "Value3");
        jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);
    }

    @Test
    public void renderAsUnsupported() {
        JobParameter jobParameter = createJobParameter(ExtendedChoiceParameterRenderer.PT_CHECKBOX);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JobParameterRenderers.ErrorLabel.class);

        jobParameter = createJobParameter(ExtendedChoiceParameterRenderer.PT_RADIO);
        jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JobParameterRenderers.ErrorLabel.class);
    }

    @Test
    public void renderAsHidden() {
        JobParameter jobParameter = createJobParameter(ExtendedChoiceParameterRenderer.PT_HIDDEN);
        JobParameterComponent<?> jobParameterComponent = jobParameterComponent = jobParameterRenderer.render(
                jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
        assertThat(jobParameterComponent.isVisible()).isFalse();
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ExtendedChoiceParameterRenderer.PT_SINGLE_SELECT))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ExtendedChoiceParameterRenderer.PT_MULTI_SELECT))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ExtendedChoiceParameterRenderer.PT_RADIO))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ExtendedChoiceParameterRenderer.PT_CHECKBOX))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ExtendedChoiceParameterRenderer.PT_TEXTBOX))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ExtendedChoiceParameterRenderer.PT_HIDDEN))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition))).isFalse();
    }

}
