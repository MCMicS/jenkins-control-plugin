package org.codinjutsu.tools.jenkins.view.action;

import org.codinjutsu.tools.jenkins.model.Job;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface JobAction {

    void execute(@NotNull Job job);
}
