package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class PatchParameterRendererTest implements JobParameterTest {

    private final PatchParameterRenderer jobParameterRenderer = new PatchParameterRenderer();

    @Test
    public void render() {
        final JobParameter jobParameter = createJobParameter(PatchParameterRenderer.TYPE);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(TextFieldWithBrowseButton.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(TextFieldWithBrowseButton.class);
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(PatchParameterRenderer.TYPE)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(new JobParameterType("PatchParameterDefinition",
                "org.jenkinsci.plugins.patch2.PatchParameterDefinition"))))
                .isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition)))
                .isFalse();
    }

}
