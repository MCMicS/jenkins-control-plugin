package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.codinjutsu.tools.jenkins.model.BuildType;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobType;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

public class ShowLogActionTest {

    private final BrowserPanel browserPanel = mock(BrowserPanel.class);
    private final AnActionEvent actionEvent = mock(AnActionEvent.class);
    private final Presentation presentation = mock(Presentation.class);
    private final Job job = createDefaultJobBuilder().build();

    @Test
    public void getActionTextForLastBuild() {
        final ShowLogAction.ShowLogActionText lastLog = ShowLogAction.getActionText(BuildType.LAST);
        assertThat(lastLog.getText()).isEqualTo("Show last log");
        assertThat(lastLog.getDescription()).isEqualTo("Show last build's log");
    }

    @Test
    public void getActionTextForLastSuccessfulBuild() {
        final ShowLogAction.ShowLogActionText lastLog = ShowLogAction.getActionText(BuildType.LAST_SUCCESSFUL);
        assertThat(lastLog.getText()).isEqualTo("Show last successful log");
        assertThat(lastLog.getDescription()).isEqualTo("Show last successful build's log");
    }

    @Test
    public void getActionTextForLastFailedBuild() {
        final ShowLogAction.ShowLogActionText lastLog = ShowLogAction.getActionText(BuildType.LAST_FAILED);
        assertThat(lastLog.getText()).isEqualTo("Show last failed log");
        assertThat(lastLog.getDescription()).isEqualTo("Show last failed build's log");
    }

    @Test
    public void updateForJobIsInQueue() {
        final ShowLogAction showLogAction = new ShowLogAction(BuildType.LAST);
        final Job jobInQueue = createDefaultJobBuilder().inQueue(true).build();
        when(browserPanel.getSelectedJob()).thenReturn(jobInQueue);
        showLogAction.update(actionEvent);
        verify(presentation).setVisible(false);
    }

    @Test
    public void updateForNoSelectedJob() {
        final ShowLogAction showLogAction = new ShowLogAction(BuildType.LAST);
        when(browserPanel.getSelectedJob()).thenReturn(null);
        showLogAction.update(actionEvent);
        verify(presentation).setVisible(false);
    }

    @Test
    public void updateForNonBuildableJob() {
        final ShowLogAction showLogAction = new ShowLogAction(BuildType.LAST);
        final Job jobNotBuildable = createDefaultJobBuilder().inQueue(true).build();
        when(browserPanel.getSelectedJob()).thenReturn(jobNotBuildable);
        showLogAction.update(actionEvent);
        verify(presentation).setVisible(false);
    }

    @Test
    public void updateForLastLogAvailable() {
        final ShowLogAction showLogAction = new ShowLogAction(BuildType.LAST);
        showLogAction.update(actionEvent);
        verify(presentation).setVisible(true);
    }

    @Test
    public void updateForLastLogNotAvailable() {
        final Job jobWithoutBuildType = createDefaultJobBuilder().inQueue(true)
                .availableBuildTypes(EnumSet.of(BuildType.LAST_SUCCESSFUL))
                .build();
        when(browserPanel.getSelectedJob()).thenReturn(jobWithoutBuildType);;
        final ShowLogAction showLogAction = new ShowLogAction(BuildType.LAST);
        showLogAction.update(actionEvent);
        verify(presentation).setVisible(false);
    }

    @Test
    public void updateForLastSuccessfulLog() {
        final ShowLogAction showLogAction = new ShowLogAction(BuildType.LAST_SUCCESSFUL);
        showLogAction.update(actionEvent);
        verify(presentation).setVisible(true);
    }

    @Test
    public void updateForLastSuccessfulLogNotAvailable() {
        final ShowLogAction showLogAction = new ShowLogAction(BuildType.LAST_SUCCESSFUL);
        final Job jobWithoutBuildType = createDefaultJobBuilder().inQueue(true)
                .availableBuildTypes(EnumSet.of(BuildType.LAST))
                .build();
        when(browserPanel.getSelectedJob()).thenReturn(jobWithoutBuildType);
        showLogAction.update(actionEvent);
        verify(presentation).setVisible(false);
    }

    @Test
    public void updateForLastFailedLog() {
        final ShowLogAction showLogAction = new ShowLogAction(BuildType.LAST_FAILED);
        showLogAction.update(actionEvent);
        verify(presentation).setVisible(true);
    }

    @Test
    public void updateForLastFailedLogNotAvailable() {
        final Job jobWithoutBuildType = createDefaultJobBuilder().inQueue(true)
                .availableBuildTypes(EnumSet.of(BuildType.LAST))
                .build();
        when(browserPanel.getSelectedJob()).thenReturn(jobWithoutBuildType);
        final ShowLogAction showLogAction = new ShowLogAction(BuildType.LAST_FAILED);
        showLogAction.update(actionEvent);
        verify(presentation).setVisible(false);
    }

    @Before
    public void setUp() {
        when(actionEvent.getPresentation()).thenReturn(presentation);
        when(browserPanel.getSelectedJob()).thenReturn(job);
    }

    @NotNull
    private static Job.JobBuilder createDefaultJobBuilder() {
        return Job.builder().name("Test").jobType(JobType.JOB).displayName("DisplayName")
                .fullName("FullName").url("http://url-to-test.com").inQueue(false).buildable(true)
                .availableBuildTypes(EnumSet.allOf(BuildType.class));
    }
}
