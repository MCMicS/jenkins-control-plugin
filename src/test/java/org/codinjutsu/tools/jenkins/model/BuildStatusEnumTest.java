package org.codinjutsu.tools.jenkins.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildStatusEnumTest {

    @Test
    public void parseStatus() {
        assertThat(BuildStatusEnum.parseStatus("failure")).isEqualTo(BuildStatusEnum.FAILURE);
        assertThat(BuildStatusEnum.parseStatus("Failure")).isEqualTo(BuildStatusEnum.FAILURE);
        assertThat(BuildStatusEnum.parseStatus("Unstable")).isEqualTo(BuildStatusEnum.UNSTABLE);
        assertThat(BuildStatusEnum.parseStatus("aborted")).isEqualTo(BuildStatusEnum.ABORTED);
        assertThat(BuildStatusEnum.parseStatus("Aborted")).isEqualTo(BuildStatusEnum.ABORTED);
        assertThat(BuildStatusEnum.parseStatus("Success")).isEqualTo(BuildStatusEnum.SUCCESS);
        assertThat(BuildStatusEnum.parseStatus("SUCCESS")).isEqualTo(BuildStatusEnum.SUCCESS);
        assertThat(BuildStatusEnum.parseStatus("folder")).isEqualTo(BuildStatusEnum.FOLDER);
        assertThat(BuildStatusEnum.parseStatus("Folder")).isEqualTo(BuildStatusEnum.FOLDER);

        assertThat(BuildStatusEnum.parseStatus("null")).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.parseStatus(null)).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.parseStatus("undefined")).isEqualTo(BuildStatusEnum.NULL);
    }

    @Test
    public void getStatus() {
        assertThat(BuildStatusEnum.getStatus("red")).isEqualTo(BuildStatusEnum.FAILURE);
        assertThat(BuildStatusEnum.getStatus("red_anime")).isEqualTo(BuildStatusEnum.FAILURE);
        assertThat(BuildStatusEnum.getStatus("yellow")).isEqualTo(BuildStatusEnum.UNSTABLE);
        assertThat(BuildStatusEnum.getStatus("yellow_anime")).isEqualTo(BuildStatusEnum.UNSTABLE);
        assertThat(BuildStatusEnum.getStatus("aborted")).isEqualTo(BuildStatusEnum.ABORTED);
        assertThat(BuildStatusEnum.getStatus("aborted_anime")).isEqualTo(BuildStatusEnum.ABORTED);
        assertThat(BuildStatusEnum.getStatus("blue")).isEqualTo(BuildStatusEnum.SUCCESS);
        assertThat(BuildStatusEnum.getStatus("blue_anime")).isEqualTo(BuildStatusEnum.SUCCESS);
        assertThat(BuildStatusEnum.getStatus("disabled")).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.getStatus("disabled_anime")).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.getStatus("nobuilt")).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.getStatus("nobuilt_anime")).isEqualTo(BuildStatusEnum.NULL);

        assertThat(BuildStatusEnum.getStatus("null")).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.getStatus(null)).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.getStatus("undefined")).isEqualTo(BuildStatusEnum.NULL);
    }
}
