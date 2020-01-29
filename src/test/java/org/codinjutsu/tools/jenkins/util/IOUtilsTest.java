package org.codinjutsu.tools.jenkins.util;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertThat;

/**
 * Created by Cezary on 2015-10-18.
 */
public class IOUtilsTest {

    public static final char SEPARATOR = '/';
    private String POLISH_TEST_STRING = "zażółć gęślą jaźń\n" +
            "ZAŻÓŁĆ GĘŚLĄ JAŹŃ";

    @Test
    public void testToStringUTF8() throws Exception {
        //Given
        InputStream inputStream = getTestResourceInputStream("test-data.utf8");
        //when
        final String result = IOUtils.toString(inputStream, "UTF-8");
        //then
        assertThat(result, IgnoreLineEndingsMatcher.equalTo(POLISH_TEST_STRING));
    }

    @Test
    public void testToStringCP1250() throws Exception {
        //Given
        InputStream inputStream = getTestResourceInputStream("test-data.cp1250");
        //when
        final String result = IOUtils.toString(inputStream, "CP1250");
        //then
        assertThat(result.replaceAll("\\r\\n?", "\n"), IgnoreLineEndingsMatcher.equalTo(POLISH_TEST_STRING));
    }

    private InputStream getTestResourceInputStream(String resource) {
        final Class<? extends IOUtilsTest> aClass = getClass();
        return aClass.getClassLoader().getResourceAsStream(aClass.getPackage().getName().replace('.', SEPARATOR) + SEPARATOR + "IOTest" + SEPARATOR + resource);
    }

    private static class IgnoreLineEndingsMatcher extends TypeSafeMatcher<String> {
        private final Matcher<String> matcher;

        private IgnoreLineEndingsMatcher(String expectedValue) {
            this.matcher = IsEqual.equalTo(expectedValue);
        }

        public static Matcher<String> equalTo(String expected) {
            return new IgnoreLineEndingsMatcher(expected);
        }

        @Override
        protected boolean matchesSafely(String s) {
            return matcher.matches(replaceLineEndings(s));
        }

        @Override
        public void describeTo(Description description) {
            matcher.describeTo(description);
        }

        private String replaceLineEndings(String content) {
            return content == null ? null : content.replaceAll("\\r\\n?", "\n");
        }
    }
}