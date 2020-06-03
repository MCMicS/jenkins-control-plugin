package org.codinjutsu.tools.jenkins.view.parameter;

import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public interface JobParameterTest {

    @NotNull
    default JobParameter createJobParameter(JobParameterType jobParameterType) {
        return createJobParameter(jobParameterType, new String [0]);
    }

    @NotNull
    default JobParameter createJobParameter(JobParameterType jobParameterType, String... nodes) {
        return JobParameter.builder().jobParameterType(jobParameterType)
                .name("Test").choices(Arrays.asList(nodes)).defaultValue("default").description("Test Parameter")
                .build();
    }
}
