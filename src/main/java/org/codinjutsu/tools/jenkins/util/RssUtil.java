/*
 * Copyright (c) 2012 David Boissier
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

import org.codinjutsu.tools.jenkins.logic.BuildStatusVisitor;
import org.codinjutsu.tools.jenkins.logic.RssBuildStatusVisitor;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RssUtil {

    private static final Pattern BUILD_NUMBER_MATCHER = Pattern.compile("#[0-9]{1,}+");

    private static final Pattern SUCCESS_MATCHER = Pattern.compile("normal|stable");

    private static final Pattern FAILED_MATCHER = Pattern.compile("failing|broken");

    private static final Pattern UNSTABLE_MATCHER = Pattern.compile("unstable");

    private static final Pattern ABORTED_MATCHER = Pattern.compile("aborted");

    private RssUtil() {
    }


    public static BuildStatusEnum extractStatus(String rssEntryTitle) {
        RssBuildStatusVisitor statusVisitor = new RssBuildStatusVisitor();
        visit(statusVisitor, rssEntryTitle);
        return statusVisitor.getStatus();
    }

    public static String extractBuildNumber(String rssEntryTitle) {
        Matcher matcher = BUILD_NUMBER_MATCHER.matcher(rssEntryTitle);
        if (matcher.find()) {
            String foundBuildNumber = matcher.group();
            return foundBuildNumber.substring(1, foundBuildNumber.length());
        }
        return null;

    }


    public static String extractBuildJob(String rssEntryTitle) {
        String[] splitStrings = BUILD_NUMBER_MATCHER.split(rssEntryTitle);
        if (splitStrings.length > 1) {
            return splitStrings[0].trim();
        }
        return null;
    }


    private static void visit(BuildStatusVisitor statusVisitor, String rssEntryTitle) {
        if (matches(rssEntryTitle, SUCCESS_MATCHER)) {
            statusVisitor.visitSuccess();
            return;
        }
        if (matches(rssEntryTitle, FAILED_MATCHER)) {
            statusVisitor.visitFailed();
            return;
        }
        if (matches(rssEntryTitle, ABORTED_MATCHER)) {
            statusVisitor.visitAborted();
            return;
        }

        if (matches(rssEntryTitle, UNSTABLE_MATCHER)) {
            statusVisitor.visitUnstable();
            return;
        }

        statusVisitor.visitUnknown();
    }

    private static boolean matches(String rssEntryTitle, Pattern pattern) {
        Matcher matcher = pattern.matcher(rssEntryTitle);
        return matcher.find();
    }

}
