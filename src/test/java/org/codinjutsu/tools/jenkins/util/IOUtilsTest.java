package org.codinjutsu.tools.jenkins.util;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;


import static org.junit.Assert.*;

/**
 * Created by Cezary on 2015-10-18.
 */
public class IOUtilsTest {

    public static final char SEPARATOR = '/';
    private String POLISH_TEST_STRING = "zażółć gęślą jaźń\r\n" +
            "ZAŻÓŁĆ GĘŚLĄ JAŹŃ";

    @Test
    public void testToStringUTF8() throws Exception {
        //Given
        InputStream inputStream = getTestResourceInputStream("test-data.utf8");
        //when
        final String result = IOUtils.toString(inputStream, "UTF-8");
        //then
        assertThat(result, CoreMatchers.equalTo(POLISH_TEST_STRING));
    }

    @Test
    public void testToStringCP1250() throws Exception {
        //Given
        InputStream inputStream = getTestResourceInputStream("test-data.cp1250");
        //when
        final String result = IOUtils.toString(inputStream, "CP1250");
        //then
        assertThat(result, CoreMatchers.equalTo(POLISH_TEST_STRING));
    }

    private InputStream getTestResourceInputStream(String resource) {
        final Class<? extends IOUtilsTest> aClass = getClass();
        return aClass.getClassLoader().getResourceAsStream(aClass.getPackage().getName().replace('.', SEPARATOR) + SEPARATOR + "IOTest" + SEPARATOR + resource);
    }
}