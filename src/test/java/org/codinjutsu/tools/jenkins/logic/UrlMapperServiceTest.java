package org.codinjutsu.tools.jenkins.logic;


import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.*;

class UrlMapperServiceTest {

    private static final String SAMPLE_URL = "http://myjenkins:7070/view/Tools/";
    private static final String EXPECTED_SAMPLE_URL = "http://myjenkins:8080/view/Tools/";
    private static final String JENKINS_URL = "http://myjenkins:7070/";
    private static final String SERVER_URL = "http://myjenkins:8080/";

    private final UrlMapperService urlMapperService = new UrlMapperService();
    private final JenkinsSettings jenkinsSettings = new JenkinsSettings();


    @BeforeEach
    void setUp() {
        jenkinsSettings.setJenkinsUrl(JENKINS_URL);
    }

    @Test
    void getUrlForSettings() {
        final var mapper = urlMapperService.getMapper(JENKINS_URL, SERVER_URL);
        assertThat(mapper).isNotNull();
        final var mappedUrl = mapper.apply(SAMPLE_URL);
        assertThat(mappedUrl).isNotNull().isEqualTo(EXPECTED_SAMPLE_URL);
    }

    @Test
    void getUrlWithoutJenkinsUrlForSettings() {
        final var mapper = urlMapperService.getMapper("", SERVER_URL);
        assertThat(mapper).isNotNull().isEqualTo(UnaryOperator.identity());
        final var mappedUrl = mapper.apply(SAMPLE_URL);
        assertThat(mappedUrl).isNotNull().isEqualTo(SAMPLE_URL);
    }

    @Test
    void getUrlForSameUrlForSettings() {
        jenkinsSettings.setJenkinsUrl(SERVER_URL);
        final var mapper = urlMapperService.getMapper(jenkinsSettings, SERVER_URL);
        assertThat(mapper).isNotNull().isEqualTo(UnaryOperator.identity());
        final var mappedUrl = mapper.apply(SAMPLE_URL);
        assertThat(mappedUrl).isNotNull().isEqualTo(SAMPLE_URL);
    }

    @Test
    void getUrl() {
        final var mapper = urlMapperService.getMapper(jenkinsSettings, SERVER_URL);
        assertThat(mapper).isNotNull();
        final var mappedUrl = mapper.apply(SAMPLE_URL);
        assertThat(mappedUrl).isNotNull().isEqualTo(EXPECTED_SAMPLE_URL);
    }

    @Test
    void getUrlWithoutJenkinsUrl() {
        jenkinsSettings.setJenkinsUrl("");
        final var mapper = urlMapperService.getMapper(jenkinsSettings, SERVER_URL);
        assertThat(mapper).isNotNull().isEqualTo(UnaryOperator.identity());
        final var mappedUrl = mapper.apply(SAMPLE_URL);
        assertThat(mappedUrl).isNotNull().isEqualTo(SAMPLE_URL);
    }

    @Test
    void getUrlForSameUrl() {
        jenkinsSettings.setJenkinsUrl(SERVER_URL);
        final var mapper = urlMapperService.getMapper(jenkinsSettings, SERVER_URL);
        assertThat(mapper).isNotNull().isEqualTo(UnaryOperator.identity());
        final var mappedUrl = mapper.apply(SAMPLE_URL);
        assertThat(mappedUrl).isNotNull().isEqualTo(SAMPLE_URL);
    }

}
