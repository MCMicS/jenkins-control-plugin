package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class HiddenParameterRendererTest implements JobParameterTest {

    private final HiddenParameterRenderer jobParameterRenderer = new HiddenParameterRenderer();
    private final JobParameterType hiddenParameter = new JobParameterType("WHideParameterDefinition",
            "com.wangyin.parameter.WHideParameterDefinition");

    @Test
    public void render() {
        final JobParameter jobParameter = createJobParameter(hiddenParameter);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
        assertThat(jobParameterComponent.isVisible()).isFalse();

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
        assertThat(jobParameterComponent.isVisible()).isFalse();
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(hiddenParameter)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(new JobParameterType("WHideParameterDefinition",
                "com.wangyin.parameter.invalid.WHideParameterDefinition"))))
                .isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition)))
                .isFalse();
    }

}
