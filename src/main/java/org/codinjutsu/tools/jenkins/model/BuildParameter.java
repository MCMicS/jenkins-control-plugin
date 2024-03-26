package org.codinjutsu.tools.jenkins.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class BuildParameter {
    @NotNull
    private String name;
    @Nullable
    private String value;
    @NotNull
    private String buildUrl;

    @NotNull
    public static BuildParameter of(@NotNull String name, @Nullable String value, @NotNull String buildUrl) {
        return new BuildParameter(name, value, buildUrl);
    }

    public String getNameToRender() {
        if (hasValue()) {
            return String.format("%s: %s", getName(), getValue());
        } else {
            return getName();
        }
    }

    public boolean hasValue() {
        return getValue() != null;
    }
}
