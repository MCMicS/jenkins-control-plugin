package org.codinjutsu.tools.jenkins.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ColorTest {

    @Test
    public void isForJobColor() {
        assertThat(Color.ABORTED.isForJobColor("Aborted")).isTrue();
        assertThat(Color.ABORTED.isForJobColor("aborted")).isTrue();
        assertThat(Color.ABORTED.isForJobColor("ABORTED")).isTrue();
        assertThat(Color.BLUE.isForJobColor("Blue")).isTrue();
        assertThat(Color.DISABLED.isForJobColor("Disabled")).isTrue();
        assertThat(Color.RED.isForJobColor("Red")).isTrue();
        assertThat(Color.YELLOW.isForJobColor("Yellow")).isTrue();
        assertThat(Color.YELLOW.isForJobColor("RedYellow")).isFalse();
    }

    @Test
    public void getJobColorName() {
        assertThat(Color.ABORTED.getJobColorName()).isEqualTo("aborted");
        assertThat(Color.BLUE.getJobColorName()).isEqualTo("blue");
        assertThat(Color.DISABLED.getJobColorName()).isEqualTo("disabled");
        assertThat(Color.RED.getJobColorName()).isEqualTo("red");
        assertThat(Color.YELLOW.getJobColorName()).isEqualTo("yellow");
    }
}
