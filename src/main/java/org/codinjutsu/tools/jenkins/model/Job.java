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

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Job {
    private final String name;
    private final String url;

    private String color;
    private boolean inQueue;

    private Health health;

    private Build lastBuild;

    private List<JobParameter> parameters = new LinkedList<JobParameter>();


    private Job(String name, String color, String url, Boolean inQueue) {
        this.name = name;
        this.color = color;
        this.url = url;
        this.inQueue = inQueue;
    }


    public static Job createJob(String jobName, String jobColor, String jobUrl, String inQueue) {
        return new Job(jobName, jobColor, jobUrl, Boolean.valueOf(inQueue));
    }


    public void updateContentWith(Job updatedJob) {
        this.color = updatedJob.getColor();
        this.health = updatedJob.getHealth();
        this.inQueue = updatedJob.isInQueue();
        this.lastBuild = updatedJob.getLastBuild();
    }


    public void addParameter(String paramName, String paramType, String defaultValue, String... choices) {
        parameters.add(JobParameter.create(paramName, paramType, defaultValue, choices));
    }

    public String getName() {
        return name;
    }


    public String getColor() {
        return color;
    }


    public String getUrl() {
        return url;
    }


    public boolean isInQueue() {
        return inQueue;
    }


    public Build getLastBuild() {
        return lastBuild;
    }


    public void setLastBuild(Build lastBuild) {
        this.lastBuild = lastBuild;
    }

    public Health getHealth() {
        return health;
    }

    public void setHealth(Health health) {
        this.health = health;
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }


    public List<JobParameter> getParameters() {
        return parameters;
    }


    public static class Health {

        private final String healthLevel;
        private final String description;

        private Health(String healthLevel, String description) {
            this.healthLevel = healthLevel;
            this.description = description;
        }

        public String getLevel() {
            return healthLevel;
        }

        public String getDescription() {
            return description;
        }

        public static Health createHealth(String healthLevel, String healthDescription) {
            return new Health(healthLevel, healthDescription);
        }
    }
}
