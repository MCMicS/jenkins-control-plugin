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
import lombok.Data;
import lombok.Singular;
import lombok.Value;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TODO mcmics: use {@link Value}
 */
@Builder
//@Value
@Data
public class Job {

    private static final Map<String, Icon> ICON_BY_JOB_HEALTH_MAP = new HashMap<>();
    public static final String WORKFLOW_JOB = "WorkflowJob";

    static {
        ICON_BY_JOB_HEALTH_MAP.put("health-00to19", GuiUtil.loadIcon("health-00to19.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-20to39", GuiUtil.loadIcon("health-20to39.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-40to59", GuiUtil.loadIcon("health-40to59.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-60to79", GuiUtil.loadIcon("health-60to79.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-80plus", GuiUtil.loadIcon("health-80plus.png"));
        ICON_BY_JOB_HEALTH_MAP.put("null", GuiUtil.loadIcon("null.png"));
    }

    @NotNull
    private final String name;
    @Builder.Default
    @NotNull
    private final JobType jobType = JobType.JOB;
    private final boolean buildable;
    @Nullable
    private final String displayName;
    @NotNull
    private final String url;
    @Singular
    @NotNull
    private final List<JobParameter> parameters;
    private boolean inQueue;
    @Nullable
    private String color;
    @Nullable
    private Health health;
    @Nullable
    private Build lastBuild;
    @Builder.Default
    @NotNull
    private List<Build> lastBuilds = new LinkedList<>();

    @Nullable
    public Icon getStateIcon() {
        return Build.getStateIcon(color);
    }

    @NotNull
    public Icon getHealthIcon() {
        if (health == null) {
            return ICON_BY_JOB_HEALTH_MAP.get("null");
        }
        return ICON_BY_JOB_HEALTH_MAP.getOrDefault(health.getLevel(), ICON_BY_JOB_HEALTH_MAP.get("null"));
    }

    @NotNull
    public String getHealthDescription() {
        if (health == null) {
            return "";
        }
        return health.getDescription();
    }

    public void updateContentWith(Job updatedJob) {
        this.color = updatedJob.getColor();
        this.health = updatedJob.getHealth();
        this.inQueue = updatedJob.isInQueue();
        this.lastBuild = updatedJob.getLastBuild();
        this.lastBuilds = updatedJob.getLastBuilds();
    }

    @NotNull
    public String getName() {
        if (StringUtils.isEmpty(displayName)) {
            return name;
        }
        return displayName;
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public boolean hasParameter(String name) {
        if (hasParameters()) {
            for (JobParameter parameter : parameters) {
                if (parameter.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Job{" +
                "name='" + name + '\'' +
                '}';
    }

    @Value
    public static class Health {

        @NotNull
        private String level;
        @Nullable
        private String description;
    }
}
