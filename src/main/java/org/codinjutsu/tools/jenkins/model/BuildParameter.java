package org.codinjutsu.tools.jenkins.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class BuildParameter {
    @NotNull
    private String name;
    @NotNull
    private String value;

    @NotNull
    public static BuildParameter of(@NotNull String name, @NotNull String value) {
        return new BuildParameter(name, value);
    }

    @Override
    @NotNull
    public String toString() {
        return String.format("%s: %s", name, value);
    }
}
