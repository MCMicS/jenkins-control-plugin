package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.openapi.ui.ComboBox;
import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.*;

public class ListGitBranchesParameterRendererTest implements JobParameterTest {

    private final ListGitBranchesParameterRenderer jobParameterRenderer = new ListGitBranchesParameterRenderer();

    @Test
    public void renderAsInputField() {
        JobParameter jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_TAG);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class); // JBTextField ?
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_BRANCH);
        jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_BRANCH_TAG);
        jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
    }

    @Test
    public void renderAsComboBox() {
        JobParameter jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_TAG, "master", "tag/v0.13.6");
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_BRANCH, "master", "bug/225");
        jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_BRANCH_TAG, "master", "tag/v0.13.6");
        jobParameterComponent = jobParameterRenderer.render(jobParameter);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(ComboBox.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition));
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
    }

    @Test
    public void isForJobParameter() {
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ListGitBranchesParameterRenderer.PT_TAG))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ListGitBranchesParameterRenderer.PT_BRANCH))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(ListGitBranchesParameterRenderer.PT_BRANCH_TAG))).isTrue();
        assertThat(jobParameterRenderer.isForJobParameter(createJobParameter(GitParameterRenderer.PT_BRANCH))).isFalse();
    }

}
