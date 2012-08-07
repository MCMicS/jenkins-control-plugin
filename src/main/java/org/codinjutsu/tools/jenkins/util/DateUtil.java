package org.codinjutsu.tools.jenkins.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static final SimpleDateFormat WORKSPACE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    public static final SimpleDateFormat RSS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final SimpleDateFormat LOG_DATE_IN_HOUR_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private DateUtil() {}

    public static Date parseDate(String buildDate, SimpleDateFormat dateFormat) {
        Date date;
        try {
            date = dateFormat.parse(buildDate);
        } catch (ParseException e) {
            System.out.println("invalid date format: " + buildDate + " with formater '" + dateFormat.toPattern() + "'");
            date = new Date();
        }
        return date;
    }

    public static String formatDateInTime(Date date) {
        return LOG_DATE_IN_HOUR_FORMAT.format(date);
    }
}
