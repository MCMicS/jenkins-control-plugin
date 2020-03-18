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
        assertThat(BuildStatusEnum.getStatusByColor("red")).isEqualTo(BuildStatusEnum.FAILURE);
        assertThat(BuildStatusEnum.getStatusByColor("red_anime")).isEqualTo(BuildStatusEnum.FAILURE);
        assertThat(BuildStatusEnum.getStatusByColor("yellow")).isEqualTo(BuildStatusEnum.UNSTABLE);
        assertThat(BuildStatusEnum.getStatusByColor("yellow_anime")).isEqualTo(BuildStatusEnum.UNSTABLE);
        assertThat(BuildStatusEnum.getStatusByColor("aborted")).isEqualTo(BuildStatusEnum.ABORTED);
        assertThat(BuildStatusEnum.getStatusByColor("aborted_anime")).isEqualTo(BuildStatusEnum.ABORTED);
        assertThat(BuildStatusEnum.getStatusByColor("blue")).isEqualTo(BuildStatusEnum.SUCCESS);
        assertThat(BuildStatusEnum.getStatusByColor("blue_anime")).isEqualTo(BuildStatusEnum.SUCCESS);
        assertThat(BuildStatusEnum.getStatusByColor("disabled")).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.getStatusByColor("disabled_anime")).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.getStatusByColor("nobuilt")).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.getStatusByColor("nobuilt_anime")).isEqualTo(BuildStatusEnum.NULL);

        assertThat(BuildStatusEnum.getStatusByColor("null")).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.getStatusByColor(null)).isEqualTo(BuildStatusEnum.NULL);
        assertThat(BuildStatusEnum.getStatusByColor("undefined")).isEqualTo(BuildStatusEnum.NULL);
    }
}
