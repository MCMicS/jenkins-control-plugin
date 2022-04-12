package org.codinjutsu.tools.jenkins.model;

import lombok.Value;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Value
public class JobParameterType {

    @NotNull
    private String type;
    @Nullable
    private String className;

    @NotNull
    public static JobParameterType getType(@NotNull String parameterType, @Nullable String parameterClass) {
        Optional<JobParameterType> jobParameter = Optional.empty();
        if (StringUtils.isEmpty(parameterClass)) {
            jobParameter = BuildInJobParameter.getBuiltInJobParameter()
                    .filter(parameter -> parameter.getType().equals(parameterType)).findFirst();
        }
        return jobParameter.orElseGet(() -> new JobParameterType(parameterType, parameterClass));
    }

    @NotNull
    public static JobParameterType createTypeForClassPrefix(@NonNls @NotNull String type,
                                                            @NonNls @NotNull String classPrefix) {
        final StringBuilder classPrefixWithTrailingDot = new StringBuilder(classPrefix);
        if (classPrefixWithTrailingDot.length() > 0) {
            if (classPrefixWithTrailingDot.charAt(classPrefixWithTrailingDot.length() - 1) != '.') {
                classPrefixWithTrailingDot.append(".");
            }
            classPrefixWithTrailingDot.append(type);
        }

        return getType(type, classPrefixWithTrailingDot.toString());
    }
}
