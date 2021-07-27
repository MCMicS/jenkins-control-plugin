package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.openapi.ui.ComboBox;
import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ExtensibleChoiceParameterRendererTest implements JobParameterTest {

    private final ExtensibleChoiceParameterRenderer jobParameterRenderer = new ExtensibleChoiceParameterRenderer();

    @Test
    public void renderAsCombobox() {
        JobParameter jobParameter = createJobParameterChoices(ExtensibleChoiceParameterRenderer.TYPE,
                "Value1", "Selected", "Value3");
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        JobParameter jobParameterWithDefault = createJobParameterChoices(ExtensibleChoiceParameterRenderer.TYPE,
                "Value1", "Selected", "Value3").toBuilder().defaultValue("Selected").build();
        jobParameterComponent = jobParameterRenderer.render(jobParameterWithDefault);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameterWithDefault);
        final ComboBox<?> comboBox = (ComboBox<?>) jobParameterComponent.getViewElement();
        assertThat(comboBox.getSelectedItem()).isEqualTo("Selected");
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ExtensibleChoiceParameterRenderer.TYPE)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(
                BuildInJobParameter.ChoiceParameterDefinition))).isFalse();
    }

}
