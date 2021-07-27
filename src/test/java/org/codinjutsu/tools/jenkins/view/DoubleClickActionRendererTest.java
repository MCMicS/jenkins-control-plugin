package org.codinjutsu.tools.jenkins.view;

import org.assertj.core.api.Assertions;
import org.codinjutsu.tools.jenkins.DoubleClickAction;
import org.junit.Test;

public class DoubleClickActionRendererTest {

    private final DoubleClickActionRenderer renderer = new DoubleClickActionRenderer();

    @Test
    public void getDisplayValue() {
        Assertions.assertThat(renderer.getDisplayValue(DoubleClickAction.TRIGGER_BUILD))
                .isEqualTo("Build on Jenkins");
        Assertions.assertThat(renderer.getDisplayValue(DoubleClickAction.LOAD_BUILDS))
                .isEqualTo("Load Builds");
        Assertions.assertThat(renderer.getDisplayValue(DoubleClickAction.SHOW_LAST_LOG))
                .isEqualTo("Show last log");
    }
}
