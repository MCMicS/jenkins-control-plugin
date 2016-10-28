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

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Job {

    private static final Map<String, Icon> ICON_BY_JOB_HEALTH_MAP = new HashMap<String, Icon>();
    private String name;

    private String displayName;
    private String url;

    private String color;
    private boolean inQueue;
    private boolean buildable;
    private boolean fetchBuild = false;

    private Health health;

    private Build lastBuild;

    private List<Build> lastBuilds = new LinkedList<>();

    private final List<JobParameter> parameters = new LinkedList<JobParameter>();

    static {
        ICON_BY_JOB_HEALTH_MAP.put("health-00to19", GuiUtil.loadIcon("health-00to19.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-20to39", GuiUtil.loadIcon("health-20to39.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-40to59", GuiUtil.loadIcon("health-40to59.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-60to79", GuiUtil.loadIcon("health-60to79.png"));
        ICON_BY_JOB_HEALTH_MAP.put("health-80plus", GuiUtil.loadIcon("health-80plus.png"));
        ICON_BY_JOB_HEALTH_MAP.put("null", GuiUtil.loadIcon("null.png"));
    }


    public Job() {
    }

    private Job(String name, String displayName, String color, String url, Boolean inQueue, Boolean buildable) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.url = url;
        this.inQueue = inQueue;
        this.buildable = buildable;
    }


    public static Job createJob(String jobName, String displayName,  String jobColor, String jobUrl, String inQueue, String buildable) {
        return new Job(jobName, displayName, jobColor, jobUrl, Boolean.valueOf(inQueue), Boolean.valueOf(buildable));
    }


    public Icon getStateIcon() {
        return Build.getStateIcon(color);
    }

    public Icon getHealthIcon() {
        if (health == null) {
            return ICON_BY_JOB_HEALTH_MAP.get("null");
        }
        return ICON_BY_JOB_HEALTH_MAP.get(health.getLevel());
    }

    public String findHealthDescription() {
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


    public void addParameter(String paramName, String paramType, String defaultValue, String... choices) {
        parameters.add(JobParameter.create(paramName, paramType, defaultValue, choices));
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (StringUtils.isEmpty(displayName)) {
            return name;
        }
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isInQueue() {
        return inQueue;
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public boolean isBuildable() {
        return buildable;
    }

    public void setBuildable(boolean buildable) {
        this.buildable = buildable;
    }

    public Build getLastBuild() {
        return lastBuild;
    }

    public void setLastBuild(Build lastBuild) {
        this.lastBuild = lastBuild;
    }

    public List<Build> getLastBuilds() {
        return lastBuilds;
    }

    public void setLastBuilds(List<Build> builds) {
        lastBuilds = builds;
    }

    Health getHealth() {
        return health;
    }

    public void setHealth(Health health) {
        this.health = health;
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public void setFetchBuild(boolean fetchBuild) {
        this.fetchBuild = fetchBuild;
    }

    public boolean isFetchBuild() {
        return fetchBuild;
    }


    public List<JobParameter> getParameters() {
        return parameters;
    }

    public boolean hasParameter(String name) {
        if (hasParameters()) {
            for(JobParameter parameter: parameters) {
                if (parameter.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setParameter(JobParameter jobParameter) {
        if (parameters.size() > 0) {
            for(JobParameter parameter: parameters) {
                if (parameter.getName().equals(jobParameter.getName())) {
                    parameters.set(parameters.indexOf(parameter), jobParameter);
                }
            }
        }
    }

    public void addParameters(List<JobParameter> jobParameters) {
        parameters.addAll(jobParameters);
    }

    public static class Health {

        private String healthLevel;
        private String description;

        public Health() {
        }

        private Health(String healthLevel, String description) {
            this.healthLevel = healthLevel;
            this.description = description;
        }

        public String getLevel() {
            return healthLevel;
        }

        public void setLevel(String healthLevel) {
            this.healthLevel = healthLevel;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public static Health createHealth(String healthLevel, String healthDescription) {
            return new Health(healthLevel, healthDescription);
        }
    }

    @Override
    public String toString() {
        return "Job{" +
                "name='" + name + '\'' +
                '}';
    }
}
