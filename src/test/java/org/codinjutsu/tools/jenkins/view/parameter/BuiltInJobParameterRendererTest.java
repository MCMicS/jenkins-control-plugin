package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.openapi.ui.ComboBox;
import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class BuiltInJobParameterRendererTest {

    private final BuiltInJobParameterRenderer jobParameterRenderer = new BuiltInJobParameterRenderer();

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.BooleanParameterDefinition)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.StringParameterDefinition)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.PasswordParameterDefinition)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.CredentialsParameterDefinition)))
                .isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.FileParameterDefinition)))
                .isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.RunParameterDefinition)))
                .isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.TextParameterDefinition)))
                .isTrue();

        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(new JobParameterType(BuildInJobParameter.ChoiceParameterDefinition.getName(), "otherClass"))))
                .isFalse();
    }

    @Test
    public void isForJobParameterForWrongBuiltInClass() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(new JobParameterType(BuildInJobParameter.ChoiceParameterDefinition.getName(), "otherClass"))))
                .isFalse();
    }

    @Test
    public void render() {
        final JobParameter jobParameter = createJobParameter(BuildInJobParameter.ChoiceParameterDefinition);
        JobParameterComponent jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.BooleanParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JCheckBox.class);
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.StringParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.PasswordParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JPasswordField.class);
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.CredentialsParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JobParameterRenderers.ErrorLabel.class);
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.FileParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JobParameterRenderers.ErrorLabel.class);
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.RunParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JobParameterRenderers.ErrorLabel.class);
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.TextParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextArea.class);
    }

    @NotNull
    private JobParameter createJobParameter(JobParameterType jobParameterType) {
        return JobParameter.builder().jobParameterType(jobParameterType)
                .name("Test").defaultValue("default").description("Test Parameter").build();
    }
}
