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

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private static final Logger LOG = Logger.getInstance(DateUtil.class.getName());

    public static final SimpleDateFormat WORKSPACE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    public static final SimpleDateFormat RSS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


    private DateUtil() {
    }

    public static Date parseDate(String buildDate, SimpleDateFormat dateFormat) {
        Date date;
        try {
            date = dateFormat.parse(buildDate);
        } catch (ParseException | NumberFormatException e) {
            LOG.debug("invalid date format: " + buildDate + " with formater '" + dateFormat.toPattern() + "'");
            date = new Date();
        } catch (Exception e) {
            LOG.error("Error while parsing data: " + buildDate, e);
            date = new Date();
        }
        return date;
    }

    /**
     * In Builds bis Jenkins 1.597 ein Zeitstempel im Format YYYY-MM-DD_hh-mm-ss.
     */
    public static boolean isValidJenkinsDate(@Nullable String buildDate) {
        try {
            Date parsedDate = null;
            if (buildDate != null && buildDate.length() > 10) {
                parsedDate = WORKSPACE_DATE_FORMAT.parse(buildDate);
            }
            return parsedDate != null;
        } catch (ParseException | NumberFormatException e) {
            return false;
        } catch (Exception e) {
            LOG.error("Error while parsing data: " + buildDate, e);
            return false;
        }
    }

}
