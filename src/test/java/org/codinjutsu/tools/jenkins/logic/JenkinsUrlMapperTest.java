package org.codinjutsu.tools.jenkins.logic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class JenkinsUrlMapperTest {

    private static final String SAMPLE_URL = "http://myjenkins:7070/view/Tools/";
    private static final String EXPECTED_SAMPLE_URL = "http://myjenkins:8080/view/Tools/";
    private final JenkinsUrlMapper jenkinsUrlMapper = new JenkinsUrlMapper("http://myjenkins:8080/",
            "http://myjenkins:7070/");

    @Test
    void mapDifferentUrl() {
        final String url = "http://otherjenkins:7070/view/Tools/";
        final var mappedUrl = jenkinsUrlMapper.apply(url);
        assertThat(mappedUrl).isNotNull().isEqualTo(url);
    }

    @Test
    void mapUrlToNew() {
        final var mappedUrl = jenkinsUrlMapper.apply(SAMPLE_URL);
        assertThat(mappedUrl).isNotNull().isEqualTo(EXPECTED_SAMPLE_URL);
    }

    @Test
    void mapNullValue() {
        final var mappedUrl = jenkinsUrlMapper.apply(null);
        assertThat(mappedUrl).isNull();
    }

    @DisplayName("Test with different ending slashes")
    @ParameterizedTest()
    @MethodSource("jenkinsUrlMapperWithoutSlash")
    void mapUrlToNewForUrlsWithoutEndingSlash(JenkinsUrlMapper jenkinsUrlMapperWithoutSlash) {
        final var mappedUrl = jenkinsUrlMapperWithoutSlash.apply(SAMPLE_URL);
        assertThat(mappedUrl).isNotNull().isEqualTo(EXPECTED_SAMPLE_URL);
    }

    static Stream<Arguments> jenkinsUrlMapperWithoutSlash() {
        return Stream.of(
                Arguments.of(Named.of("ServerUrl without ending slash",
                        new JenkinsUrlMapper("http://myjenkins:8080", "http://myjenkins:7070/"))),
                Arguments.of(Named.of("JenkinsUrl without ending slash",
                        new JenkinsUrlMapper("http://myjenkins:8080/", "http://myjenkins:7070"))),
                Arguments.of(Named.of("Both urls without ending slash",
                        new JenkinsUrlMapper("http://myjenkins:8080", "http://myjenkins:7070")))
        );
    }
}
