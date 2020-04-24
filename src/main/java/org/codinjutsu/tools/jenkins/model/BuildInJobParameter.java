package org.codinjutsu.tools.jenkins.model;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public final class BuildInJobParameter {

    public static final JobParameterType ChoiceParameterDefinition = create("ChoiceParameterDefinition");
    public static final JobParameterType BooleanParameterDefinition = create("BooleanParameterDefinition");
    public static final JobParameterType CredentialsParameterDefinition = create("CredentialsParameterDefinition");
    public static final JobParameterType FileParameterDefinition = create("FileParameterDefinition");
    public static final JobParameterType PasswordParameterDefinition = create("PasswordParameterDefinition");
    public static final JobParameterType RunParameterDefinition = create("RunParameterDefinition");
    public static final JobParameterType TextParameterDefinition = create("TextParameterDefinition");
    public static final JobParameterType StringParameterDefinition = create("StringParameterDefinition");

    @NotNull
    private static JobParameterType create(@NotNull String name) {
        return new JobParameterType(name, "hudson.model." + name);
    }

    @Nonnull
    public static Stream<JobParameterType> getBuiltInJobParameter() {
        return Stream.of(ChoiceParameterDefinition, BooleanParameterDefinition, CredentialsParameterDefinition,
                FileParameterDefinition, PasswordParameterDefinition, RunParameterDefinition, TextParameterDefinition,
                StringParameterDefinition);
    }

}
