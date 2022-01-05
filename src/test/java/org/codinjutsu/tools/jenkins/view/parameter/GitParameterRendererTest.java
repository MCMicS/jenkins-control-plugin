package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.openapi.ui.ComboBox;
import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class GitParameterRendererTest implements JobParameterTest {
    private final GitParameterRenderer jobParameterRenderer = new GitParameterRenderer();

    @Test
    public void renderAsInputField() {
        JobParameter jobParameter = createJobParameter(GitParameterRenderer.PT_TAG);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class); // JBTextField ?
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(GitParameterRenderer.PT_BRANCH);
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(GitParameterRenderer.PT_BRANCH_TAG);
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(GitParameterRenderer.PT_REVISION);
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(GitParameterRenderer.PT_PULL_REQUEST);
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition), PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
    }

    @Test
    public void renderAsComboBox() {
        JobParameter jobParameter = createJobParameterChoices(GitParameterRenderer.PT_TAG, "master", "tag/v0.13.6");
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameterChoices(GitParameterRenderer.PT_BRANCH, "master", "bug/225");
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameterChoices(GitParameterRenderer.PT_BRANCH_TAG, "master", "tag/v0.13.6");
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameterChoices(GitParameterRenderer.PT_REVISION,
                "abcf12345 2020-04-14 21:36 user <user@users.noreply.github.com> sample Revision");
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameterChoices(GitParameterRenderer.PT_PULL_REQUEST, "master", "pr/226");
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(GitParameterRenderer.PT_TAG))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(GitParameterRenderer.PT_BRANCH))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(GitParameterRenderer.PT_BRANCH_TAG))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(GitParameterRenderer.PT_REVISION))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(GitParameterRenderer.PT_PULL_REQUEST))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(new JobParameterType(//
                GitParameterRenderer.PT_TAG.getName(), "net.uaznia.lukanus.hudson.plugins.gitparameter.invalid.GitParameterDefinition"))))
                .isFalse();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition)))
                .isFalse();
    }

}
