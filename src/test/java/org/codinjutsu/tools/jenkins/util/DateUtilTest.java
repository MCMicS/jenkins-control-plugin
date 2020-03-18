package org.codinjutsu.tools.jenkins.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilTest {

    @Test
    public void isValidJenkinsDate() {
        assertThat(DateUtil.isValidJenkinsDate("18-1")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("18-10")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-12")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-02")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-10")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-100")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-1")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("12301-01-010")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("200-010")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("2005-01-22")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("12345")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("12345-12")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-100")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("1234-001")).isFalse();
        assertThat(DateUtil.isValidJenkinsDate("16359")).isFalse();


        assertThat(DateUtil.isValidJenkinsDate("2020-02-20_21-54-26")).isTrue();
        assertThat(DateUtil.isValidJenkinsDate("20-02-20_21-54-26")).isTrue();
    }
}
