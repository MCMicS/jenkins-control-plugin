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
    public static BuildParameter of(@NotNull String name, @Nullable String value) {
        return new BuildParameter(name, value);
    }
}
