package org.codinjutsu.tools.jenkins.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BuildParameter {
    public static BuildParameter of() {
        return new BuildParameter();
    }

    private String name;

    private String value;
}
