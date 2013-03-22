/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static final SimpleDateFormat WORKSPACE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    public static final SimpleDateFormat RSS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final SimpleDateFormat LOG_DATE_IN_HOUR_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private DateUtil() {
    }

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
