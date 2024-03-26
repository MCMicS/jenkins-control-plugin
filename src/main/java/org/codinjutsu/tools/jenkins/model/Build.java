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

import com.intellij.ide.nls.NlsMessages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.DateFormatUtil;
import lombok.Builder;
import lombok.Value;
import org.codinjutsu.tools.jenkins.util.DateUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
    @Nullable
    private final String displayName = null;
    @Builder.Default
    @Nullable
    private final String fullDisplayName = null;
    @Builder.Default
    private final Date timestamp = new Date();
    @Nullable
    @Builder.Default
    private final Long duration = null;
    @NotNull
    @Builder.Default
    private final BuildStatusEnum status = BuildStatusEnum.NULL;
    @Builder.Default
    @NotNull
    private final List<BuildParameter> buildParameterList = new LinkedList<>();

    @SuppressWarnings("java:S107")
    @NotNull
    public static Build createBuildFromWorkspace(String buildUrl, int number, String status, boolean isBuilding,
                                                 String buildDate, Long timestamp, Long duration, SimpleDateFormat dateFormat) {
        return createBuild(buildUrl, number, status, isBuilding, buildDate, dateFormat, null, timestamp, duration);
    }

    @SuppressWarnings("java:S107")
    @NotNull
    public static Build createBuildFromRss(String buildUrl, int number, String status, boolean isBuilding,
                                           String buildDate, String message, SimpleDateFormat dateFormat) {
        return createBuild(buildUrl, number, status, isBuilding, buildDate, dateFormat, message, 0L, 0L);
    }

    @SuppressWarnings("java:S107")
    @NotNull
    public static Build createBuild(com.offbytwo.jenkins.model.Build build) {
        final BuildStatusEnum status = BuildStatusEnum.NULL;
        //boolean isBuilding = build.details().isBuilding()
        boolean isBuilding = false;
        String message = null;
        return createBuild(build.getUrl(), build.getNumber(), status.getStatus(), isBuilding, message, 0L, 0L)
                .build();
    }

    @SuppressWarnings("java:S107")
    private static BuildBuilder createBuild(String buildUrl, int number, String status, boolean isBuilding, String message,
                                            Long timestamp, Long duration) {
        return Build.builder()
                .url(buildUrl)
                .number(number)
                .status(BuildStatusEnum.parseStatus(status))
                .building(isBuilding)
                .message(message)
                .timestamp(new Date(timestamp))
                .duration(duration);
    }

    @SuppressWarnings("java:S107")
    @NotNull
    private static Build createBuild(String buildUrl, int number, String status, boolean isBuilding, String buildDate,
                                     SimpleDateFormat simpleDateFormat, String message, Long timestamp, Long duration) {
        return createBuild(buildUrl, number, status, isBuilding, message, timestamp, duration)
                .buildDate(DateUtil.parseDate(buildDate, simpleDateFormat))
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

    @NotNull
    public String getDisplayNumber() {
        return Optional.ofNullable(getDisplayName()).orElseGet(() -> "#" + getNumber());
    }

    @NotNull
    public String getNameToRender() {
        return Optional.ofNullable(getFullDisplayName())
                .filter(StringUtil::isNotEmpty)
                .orElseGet(() -> String.format("%s (%s)", getDisplayNumber(),
                        DateFormatUtil.formatDateTime(getTimestamp())));
    }

    @NotNull
    public String getNameToRenderWithDuration() {
        var runningStatus = isBuilding() ? " (running)" : "";

        final Optional<Long> buildDuration = Optional.ofNullable(getDuration());
        if(buildDuration.isPresent()) {
            return String.format("%s duration: %s%s", getNameToRender(),
                    NlsMessages.formatDuration(buildDuration.get()), runningStatus);
        }
        return getNameToRender();
    }

    public boolean isAfter(Build aBuild) {
        return this.getNumber() > aBuild.getNumber();
    }
}
