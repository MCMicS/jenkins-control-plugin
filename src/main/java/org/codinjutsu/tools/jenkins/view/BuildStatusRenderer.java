package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@FunctionalInterface
public interface BuildStatusRenderer {

    @NotNull
    Icon renderBuildStatus(@NotNull BuildStatusEnum buildStatus);
}
