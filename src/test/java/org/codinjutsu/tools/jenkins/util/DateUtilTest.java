package org.codinjutsu.tools.jenkins.util;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilTest {

    private final SimpleDateFormat workspaceDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    @Test
    public void isValidJenkinsDate() {
        assertThat(DateUtil.isValidJenkinsDate("18-1", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("18-10", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-12", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-02", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-10", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-100", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-1", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("12301-01-010", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("200-010", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("2005-01-22", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("12345", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("12345-12", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-100", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-001", workspaceDateFormat)).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("16359", workspaceDateFormat)).isFalse();


        assertThat(DateUtil.isValidJenkinsDate("2020-02-20_21-54-26", workspaceDateFormat)).isTrue();
        assertThat(DateUtil.isValidJenkinsDate("20-02-20_21-54-26", workspaceDateFormat)).isTrue();
    }
}
