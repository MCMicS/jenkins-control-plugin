package org.codinjutsu.tools.jenkins.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class JobParameterTypeTest {

    private static final String EMPTY_CLASS = "";

    @Test
    public void getBuildInTypesIfClassIsMissing() {
        assertThat(JobParameterType.getType("ChoiceParameterDefinition", EMPTY_CLASS))
                .isEqualTo(BuildInJobParameter.ChoiceParameterDefinition);
        assertThat(JobParameterType.getType("BooleanParameterDefinition", EMPTY_CLASS))
                .isEqualTo(BuildInJobParameter.BooleanParameterDefinition);
        assertThat(JobParameterType.getType("CredentialsParameterDefinition", EMPTY_CLASS))
                .isEqualTo(BuildInJobParameter.CredentialsParameterDefinition);
        assertThat(JobParameterType.getType("FileParameterDefinition", EMPTY_CLASS))
                .isEqualTo(BuildInJobParameter.FileParameterDefinition);
        assertThat(JobParameterType.getType("PasswordParameterDefinition", EMPTY_CLASS))
                .isEqualTo(BuildInJobParameter.PasswordParameterDefinition);
        assertThat(JobParameterType.getType("RunParameterDefinition", EMPTY_CLASS))
                .isEqualTo(BuildInJobParameter.RunParameterDefinition);
        assertThat(JobParameterType.getType("TextParameterDefinition", EMPTY_CLASS))
                .isEqualTo(BuildInJobParameter.TextParameterDefinition);
        assertThat(JobParameterType.getType("StringParameterDefinition", EMPTY_CLASS))
                .isEqualTo(BuildInJobParameter.StringParameterDefinition);
    }

    @Test
    public void getCustomType() {
        assertThat(JobParameterType.getType("ParameterSeparatorDefinition",
                "jenkins.plugins.parameter_separator.ParameterSeparatorDefinition"))
                .isEqualTo(new JobParameterType("ParameterSeparatorDefinition",
                        "jenkins.plugins.parameter_separator.ParameterSeparatorDefinition"));
    }
}
