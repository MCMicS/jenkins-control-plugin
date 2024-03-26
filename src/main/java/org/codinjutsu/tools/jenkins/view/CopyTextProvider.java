package org.codinjutsu.tools.jenkins.view;


import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@FunctionalInterface
public interface CopyTextProvider {
    default @NotNull Collection<String> getTextLinesToCopy() {
        return getTextToCopy().map(Collections::singletonList).orElseGet(Collections::emptyList);
    }

    @NotNull Optional<String> getTextToCopy();
}
