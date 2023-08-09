package org.codinjutsu.tools.jenkins.logic;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.codinjutsu.tools.jenkins.logic.RangeToLoad.NO_RESTRICTION;

public class RangeToLoadTest {

    @Test
    public void to() {
        assertThat(RangeToLoad.to(5)).isEqualTo(new RangeToLoad(NO_RESTRICTION, 5));
        assertThat(RangeToLoad.to(0)).isEqualTo(new RangeToLoad(NO_RESTRICTION, 0));
        assertThat(RangeToLoad.to(-1)).isEqualTo(new RangeToLoad(NO_RESTRICTION, NO_RESTRICTION));
    }

    @Test
    public void range() {
        assertThat(RangeToLoad.range(5, 8)).isEqualTo(new RangeToLoad(5, 8));
        assertThat(RangeToLoad.range(8, 5)).isEqualTo(new RangeToLoad(8, NO_RESTRICTION));
        assertThat(RangeToLoad.range(8, 8)).isEqualTo(new RangeToLoad(8, 8));
        assertThat(RangeToLoad.range(8, 9)).isEqualTo(new RangeToLoad(8, 9));
        assertThat(RangeToLoad.range(-1, 8)).isEqualTo(new RangeToLoad(NO_RESTRICTION, 8));
        assertThat(RangeToLoad.range(8, -1)).isEqualTo(new RangeToLoad(8, NO_RESTRICTION));
    }

    @Test
    public void from() {
        assertThat(RangeToLoad.from(5)).isEqualTo(new RangeToLoad(5, NO_RESTRICTION));
        assertThat(RangeToLoad.from(0)).isEqualTo(new RangeToLoad(0, NO_RESTRICTION));
        assertThat(RangeToLoad.from(-1)).isEqualTo(new RangeToLoad(0, NO_RESTRICTION));
    }

    @Test
    public void only() {
        assertThat(RangeToLoad.only(5)).isEqualTo(new RangeToLoad(5, 6));
        assertThat(RangeToLoad.only(0)).isEqualTo(new RangeToLoad(0, 1));
        assertThat(RangeToLoad.only(-1)).isEqualTo(new RangeToLoad(0, 1));
    }

    @Test
    public void toQueryParameter() {
        assertThat(RangeToLoad.range(5, 8).toQueryParameter()).isEqualTo("{5,8}");
        assertThat(RangeToLoad.range(8, 5).toQueryParameter()).isEqualTo("{8,}");
        assertThat(RangeToLoad.range(-1, 8).toQueryParameter()).isEqualTo("{,8}");
        assertThat(RangeToLoad.range(8, -1).toQueryParameter()).isEqualTo("{8,}");
        assertThat(RangeToLoad.range(8, 8).toQueryParameter()).isEqualTo("{8,8}");
        // same as {8,9}
        assertThat(RangeToLoad.range(8, 9).toQueryParameter()).isEqualTo("{8}");

        assertThat(RangeToLoad.from(5).toQueryParameter()).isEqualTo("{5,}");
        assertThat(RangeToLoad.from(0).toQueryParameter()).isEqualTo("{0,}");
        assertThat(RangeToLoad.from(-1).toQueryParameter()).isEqualTo("{0,}");

        assertThat(RangeToLoad.to(5).toQueryParameter()).isEqualTo("{,5}");
        assertThat(RangeToLoad.to(0).toQueryParameter()).isEmpty();
        assertThat(RangeToLoad.to(-1).toQueryParameter()).isEmpty();

        assertThat(RangeToLoad.only(5).toQueryParameter()).isEqualTo("{5}");
        assertThat(RangeToLoad.only(0).toQueryParameter()).isEqualTo("{0}");
        assertThat(RangeToLoad.only(-1).toQueryParameter()).isEqualTo("{0}");

        // Custom Values
        assertThat(new RangeToLoad(-1, 2).toQueryParameter()).isEqualTo("{-1,2}");
        assertThat(new RangeToLoad(-1, 0).toQueryParameter()).isEqualTo("{-1}");
        assertThat(new RangeToLoad(-1, 1).toQueryParameter()).isEqualTo("{-1,1}");
        assertThat(new RangeToLoad(5, 2).toQueryParameter()).isEqualTo("{5}");
    }
}
