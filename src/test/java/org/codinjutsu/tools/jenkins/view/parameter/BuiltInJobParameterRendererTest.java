package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class BuiltInJobParameterRendererTest implements JobParameterTest {

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
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.FileParameterDefinition)))
                .isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.CredentialsParameterDefinition)))
                .isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.RunParameterDefinition)))
                .isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.TextParameterDefinition)))
                .isTrue();

        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(new JobParameterType(
                BuildInJobParameter.ChoiceParameterDefinition.getType(), "otherClass")))).isFalse();
    }

    @Test
    public void isForJobParameterForWrongBuiltInClass() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(new JobParameterType(
                BuildInJobParameter.ChoiceParameterDefinition.getType(), "otherClass")))).isFalse();
    }

    @Test
    public void render() {
        final JobParameter jobParameter = createJobParameter(BuildInJobParameter.ChoiceParameterDefinition);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.BooleanParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JCheckBox.class);
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.StringParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        final PasswordComponent passwordComponent = PasswordComponent.create();
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.PasswordParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(passwordComponent.asComponent().getClass());
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.CredentialsParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JobParameterRenderers.ErrorLabel.class);
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.FileParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(TextFieldWithBrowseButton.class);
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.RunParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JobParameterRenderers.ErrorLabel.class);
        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.TextParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextArea.class);
    }
}
