package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class ReadonlyParameterRendererTest implements JobParameterTest {

    private final ReadonlyParameterRenderer jobParameterRenderer = new ReadonlyParameterRenderer();

    @Test
    public void render() {
        final JobParameter jobParameter = createJobParameter(ReadonlyParameterRenderer.STRING);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getViewElement().isEditable()).isFalse();
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(ReadonlyParameterRenderer.TEXT), PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextArea.class);
        assertThat(jobParameterComponent.getViewElement().isEditable()).isFalse();

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition), PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JobParameterRenderers.ErrorLabel.class);
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ReadonlyParameterRenderer.STRING)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ReadonlyParameterRenderer.TEXT)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(new JobParameterType("ReadonlyStringParameterDefinition",
                "com.gem.readonlyparameterInvalid.ReadonlyStringParameterDefinition"))))
                .isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition)))
                .isFalse();
    }

}
