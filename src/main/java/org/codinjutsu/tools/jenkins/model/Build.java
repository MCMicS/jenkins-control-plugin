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

package org.codinjutsu.tools.jenkins.model;

import org.codinjutsu.tools.jenkins.util.DateUtil;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Build {

    private static final Map<BuildStatusEnum, Icon> ICON_BY_BUILD_STATUS_MAP = new HashMap<BuildStatusEnum, Icon>();

    private final String url;
    private final Date buildDate;
    private final int number;
    private final boolean building;
    private final String message;

    private final BuildStatusEnum status;

    static {
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.SUCCESS, GuiUtil.loadIcon("blue.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.STABLE, GuiUtil.loadIcon("blue.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.FAILURE, GuiUtil.loadIcon("red.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.UNSTABLE, GuiUtil.loadIcon("yellow.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.NULL, GuiUtil.loadIcon("grey.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.ABORTED, GuiUtil.loadIcon("grey.png"));
    }


    private Build(String url, int number, Date buildDate, BuildStatusEnum status, boolean isBuilding, String message) {
        this.url = url;
        this.number = number;
        this.buildDate = buildDate;
        this.status = status;
        this.building = isBuilding;
        this.message = message;
    }

    public static Icon getStateIcon(String jobColor) {
        BuildStatusEnum[] jobStates = BuildStatusEnum.values();
        for (BuildStatusEnum jobState : jobStates) {
            String stateName = jobState.getColor();
            if (jobColor.startsWith(stateName)) {
                return ICON_BY_BUILD_STATUS_MAP.get(jobState);
            }
        }

        return ICON_BY_BUILD_STATUS_MAP.get(BuildStatusEnum.NULL);
    }


    public String getUrl() {
        return url;
    }


    public int getNumber() {
        return number;
    }


    public BuildStatusEnum getStatus() {
        return status;
    }


    public String getStatusValue() {
        return status.getStatus();
    }

    public Date getBuildDate() {
        return buildDate;
    }


    public boolean isBuilding() {
        return building;
    }


    public boolean isAfter(Build aBuild) {
        return this.getNumber() > aBuild.getNumber();
    }


    public static Build createBuildFromWorkspace(String buildUrl, String number, String status, String isBuilding, String buildDate) {
        return createBuild(buildUrl, number, status, isBuilding, buildDate, DateUtil.WORKSPACE_DATE_FORMAT, "Dummy message");
    }

    public static Build createBuildFromRss(String buildUrl, String number, String status, String isBuilding, String buildDate, String message) {
        return createBuild(buildUrl, number, status, isBuilding, buildDate, DateUtil.RSS_DATE_FORMAT, message);
    }


    private static Build createBuild(String buildUrl, String number, String status, String isBuilding, String buildDate, SimpleDateFormat simpleDateFormat, String message) {
        BuildStatusEnum buildStatusEnum = BuildStatusEnum.parseStatus(status);
        Date date = DateUtil.parseDate(buildDate, simpleDateFormat);

        return new Build(buildUrl, Integer.valueOf(number), date, buildStatusEnum, Boolean.valueOf(isBuilding), message);
    }

    public String getMessage() {
        return message;
    }
}
