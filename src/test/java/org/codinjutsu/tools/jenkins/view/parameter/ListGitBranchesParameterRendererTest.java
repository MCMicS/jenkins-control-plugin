package org.codinjutsu.tools.jenkins.view.parameter;

import org.assertj.core.util.Lists;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.BuildInJobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.view.inputfilter.InputFilterList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ListGitBranchesParameterRendererTest implements JobParameterTest {

    private final ListGitBranchesParameterRenderer jobParameterRenderer = new ListGitBranchesParameterRenderer();

    private final RequestManager requestManager = Mockito.mock(RequestManager.class);

    @Before
    public void setUp() {
        Mockito.when(PROJECT_JOB.getProject().getService(RequestManager.class))
                .thenReturn(requestManager);
        Mockito.when(requestManager.getGitParameterChoices(Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    public void renderAsInputField() {
        JobParameter jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_TAG);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class); // JBTextField ?
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_BRANCH);
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_BRANCH_TAG);
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JTextField.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
    }

    @Test
    public void renderAsInputFilterList() {
        JobParameter jobParameter = createJobParameterChoices(ListGitBranchesParameterRenderer.PT_TAG, "master", "tag/v0.13.6");
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(InputFilterList.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameterChoices(ListGitBranchesParameterRenderer.PT_BRANCH, "master", "bug/225");
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(InputFilterList.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameter = createJobParameterChoices(ListGitBranchesParameterRenderer.PT_BRANCH_TAG, "master", "tag/v0.13.6");
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(InputFilterList.class);
        assertThat(jobParameterComponent.getJobParameter()).isEqualTo(jobParameter);

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition),
                PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(JLabel.class);
    }

    @Test
    public void loadFromRequestManager() {
        Mockito.when(requestManager.getGitParameterChoices(Mockito.any(), Mockito.any()))
                .thenReturn(Lists.newArrayList("First", "Second"));
        JobParameter jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_TAG);
        JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(InputFilterList.class);
        assertThat(jobParameterComponent.getJobParameter().getChoices()).contains("First", "Second");

        jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_BRANCH);
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(InputFilterList.class);
        assertThat(jobParameterComponent.getJobParameter().getChoices()).contains("First", "Second");

        jobParameter = createJobParameter(ListGitBranchesParameterRenderer.PT_BRANCH_TAG);
        jobParameterComponent = jobParameterRenderer.render(jobParameter, PROJECT_JOB);
        assertThat(jobParameterComponent.getViewElement()).isInstanceOf(InputFilterList.class);
        assertThat(jobParameterComponent.getJobParameter().getChoices()).contains("First", "Second");

        jobParameterComponent = jobParameterRenderer.render(createJobParameter(BuildInJobParameter.ChoiceParameterDefinition),
                PROJECT_JOB);
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
