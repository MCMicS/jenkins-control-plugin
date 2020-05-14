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

package org.codinjutsu.tools.jenkins.model;

import lombok.Builder;
import lombok.Value;
import org.codinjutsu.tools.jenkins.util.DateUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

@Value
@Builder(toBuilder = true)
public class Build {

    public static final Build NULL = createNullBuild();

    @NotNull
    private final String url;
    @NotNull
    @Builder.Default
    private final Date buildDate = new Date();
    private final int number;
    private final boolean building;
    @Nullable
    @Builder.Default
    private final String message = null;
    @Builder.Default
    private final Date timestamp = new Date();
    @Nullable
    @Builder.Default
    private final Long duration = null;
    @NotNull
    @Builder.Default
    private final BuildStatusEnum status = BuildStatusEnum.NULL;

    @NotNull
    public static Build createBuildFromWorkspace(String buildUrl, int number, String status, boolean isBuilding, String buildDate, Long timestamp, Long duration) {
        return createBuild(buildUrl, number, status, isBuilding, buildDate, DateUtil.WORKSPACE_DATE_FORMAT, null, timestamp, duration);
    }

    @NotNull
    public static Build createBuildFromRss(String buildUrl, int number, String status, boolean isBuilding, String buildDate, String message) {
        return createBuild(buildUrl, number, status, isBuilding, buildDate, DateUtil.RSS_DATE_FORMAT, message, 0L, 0L);
    }

    @NotNull
    private static Build createBuild(String buildUrl, int number, String status, boolean isBuilding, String buildDate, SimpleDateFormat simpleDateFormat, String message, Long timestamp, Long duration) {
        return Build.builder()
                .url(buildUrl)
                .number(number)
                .buildDate(DateUtil.parseDate(buildDate, simpleDateFormat))
                .status(BuildStatusEnum.parseStatus(status))
                .building(isBuilding)
                .message(message)
                .timestamp(new Date(timestamp))
                .duration(duration)
                .build();
    }

    @NotNull
    private static Build createNullBuild() {
        final Date defaultDate = new Date();
        return Build.builder()
                .url("http://dev.null")
                .number(0)
                .buildDate(defaultDate)
                .building(false)
                .message("")
                .timestamp(defaultDate)
                .build();
    }

    public boolean isAfter(Build aBuild) {
        return this.getNumber() > aBuild.getNumber();
    }
}
