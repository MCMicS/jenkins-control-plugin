package org.codinjutsu.tools.jenkins.util;

public class StringUtil {
    public static final String EMPTY = "";

    private StringUtil() {
        // init
    }

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
