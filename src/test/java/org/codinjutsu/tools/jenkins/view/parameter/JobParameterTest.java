package org.codinjutsu.tools.jenkins.view.parameter;

import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.codinjutsu.tools.jenkins.model.ProjectJob;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

public interface JobParameterTest {

    ProjectJob PROJECT_JOB = ProjectJob.builder().project(mock(Project.class)).job(mock(Job.class)).build();

    @NotNull
    default JobParameter createJobParameter(JobParameterType jobParameterType) {
        return createJobParameter(jobParameterType, "");
    }

    @NotNull
    default JobParameter createJobParameter(JobParameterType jobParameterType, String defaultValue) {
        return createJobParameter(jobParameterType, defaultValue, new String [0]);
    }

    @NotNull
    default JobParameter createJobParameterChoices(JobParameterType jobParameterType, String... choices) {
        return createJobParameter(jobParameterType, "", choices);
    }

    @NotNull
    default JobParameter createJobParameter(JobParameterType jobParameterType, String defaultValue,
                                            String... choices) {
        return createJobParameterBuilder(jobParameterType)
                .defaultValue(defaultValue).choices(Arrays.asList(choices))
                .build();
    }

    @NotNull
    static JobParameter.JobParameterBuilder createJobParameterBuilder(JobParameterType jobParameterType) {
        return JobParameter.builder()
                .jobParameterType(jobParameterType)
                .name("Test")
                .description("Test Parameter");
    }
}
