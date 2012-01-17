/*
 * Copyright (c) 2011 David Boissier
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

import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.SUCCESS;

public class Build {

    private static final Map<BuildStatusEnum, Icon> ICON_BY_BUILD_STATUS_MAP = new HashMap<BuildStatusEnum, Icon>();

    private final String buildUrl;
    private final int number;
    private final boolean building;

    private final BuildStatusEnum status;

    static {
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.SUCCESS, GuiUtil.loadIcon("blue.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.STABLE, GuiUtil.loadIcon("blue.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.FAILURE, GuiUtil.loadIcon("red.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.UNSTABLE, GuiUtil.loadIcon("yellow.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.NULL, GuiUtil.loadIcon("grey.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.ABORTED, GuiUtil.loadIcon("grey.png"));
    }


    private Build(String buildUrl, int number, BuildStatusEnum status, boolean isBuilding) {
        this.buildUrl = buildUrl;
        this.number = number;
        this.status = status;
        this.building = isBuilding;
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
        return buildUrl;
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


    boolean isSuccess() {
        return SUCCESS.equals(status);
    }


    public boolean isBuilding() {
        return building;
    }


    public boolean isDisplayable(Build currentBuild) {
        return this.isAfter(currentBuild) &&
                (BuildStatusEnum.ABORTED.equals(this.getStatus()) || this.hasNotSameSuccessThan(currentBuild));
    }


    public boolean isAfter(Build aBuild) {
        return this.getNumber() > aBuild.getNumber();
    }


    private boolean hasNotSameSuccessThan(Build aBuild) {
        return !(this.isSuccess() && aBuild.isSuccess());
    }


    public static Build createBuild(String buildUrl, String number, String status, String isBuilding) {
        if (status == null || "null".equals(status)) {
            status = "NULL";
        }

        BuildStatusEnum buildStatusEnum;
        try {
            buildStatusEnum = BuildStatusEnum.valueOf(status.toUpperCase());

        } catch (IllegalArgumentException ex) {
            System.out.println("Unkown status : " + status);
            buildStatusEnum = BuildStatusEnum.NULL;
        }
        return new Build(buildUrl, Integer.valueOf(number), buildStatusEnum, Boolean.valueOf(isBuilding));
    }
}
