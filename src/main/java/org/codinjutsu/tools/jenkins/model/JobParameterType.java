package org.codinjutsu.tools.jenkins.model;

import lombok.Value;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Value
public class JobParameterType {

    @NotNull
    private String name;
    @Nullable
    private String className;

    @NotNull
    public static JobParameterType getType(@NotNull String parameterName, @Nullable String parameterClass) {
        Optional<JobParameterType> jobParameter = Optional.empty();
        if (StringUtils.isEmpty(parameterClass)) {
            jobParameter = BuildInJobParameter.getBuiltInJobParameter()
                    .filter(parameter -> parameter.getName().equals(parameterName)).findFirst();
        }
        return jobParameter.orElseGet(() -> new JobParameterType(parameterName, parameterClass));
    }
}
