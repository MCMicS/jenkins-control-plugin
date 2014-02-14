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

import org.apache.commons.httpclient.util.URIUtil;
import org.codinjutsu.tools.jenkins.util.DateUtil;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Build {

    public static final Map<BuildStatusEnum, Icon> ICON_BY_BUILD_STATUS_MAP = new HashMap<BuildStatusEnum, Icon>();

    private String url;
    private Date buildDate;
    private int number;
    private boolean building;
    private String message;

    private BuildStatusEnum status;

    static {
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.SUCCESS, GuiUtil.loadIcon("blue.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.STABLE, GuiUtil.loadIcon("blue.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.FAILURE, GuiUtil.loadIcon("red.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.UNSTABLE, GuiUtil.loadIcon("yellow.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.NULL, GuiUtil.loadIcon("grey.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.ABORTED, GuiUtil.loadIcon("grey.png"));
    }


    public static Build createBuildFromWorkspace(String buildUrl, Long number, String status, Boolean isBuilding, String buildDate) {
        return createBuild(buildUrl, number, status, isBuilding, buildDate, DateUtil.WORKSPACE_DATE_FORMAT, null);
    }

    public static Build createBuildFromWorkspace(String buildUrl, String number, String status, String isBuilding, String buildDate) {
        return createBuild(buildUrl, Long.parseLong(number), status, Boolean.parseBoolean(isBuilding), buildDate, DateUtil.WORKSPACE_DATE_FORMAT, null);
    }

    public static Build createBuildFromRss(String buildUrl, String number, String status, String isBuilding, String buildDate, String message) {
        return createBuild(buildUrl, Long.parseLong(number), status, Boolean.parseBoolean(isBuilding), buildDate, DateUtil.RSS_DATE_FORMAT, message);
    }

    private static Build createBuild(String buildUrl, Long number, String status, Boolean isBuilding, String buildDate, SimpleDateFormat simpleDateFormat, String message) {
        BuildStatusEnum buildStatusEnum = BuildStatusEnum.parseStatus(status);
        Date date = DateUtil.parseDate(buildDate, simpleDateFormat);

        return new Build(buildUrl, number.intValue(), date, buildStatusEnum, isBuilding, message);
    }

    public Build() {
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

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public BuildStatusEnum getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = BuildStatusEnum.parseStatus(status);
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = DateUtil.parseDate(buildDate, DateUtil.WORKSPACE_DATE_FORMAT);
    }


    public void setBuildDate(String buildDate, SimpleDateFormat dateFormat) {
        this.buildDate = DateUtil.parseDate(buildDate, dateFormat);
    }


    public boolean isBuilding() {
        return building;
    }

    public void setBuilding(boolean building) {
        this.building = building;
    }

    public boolean isAfter(Build aBuild) {
        return this.getNumber() > aBuild.getNumber();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
