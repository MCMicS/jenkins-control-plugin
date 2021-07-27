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

package org.codinjutsu.tools.jenkins.logic;

import org.codinjutsu.tools.jenkins.model.*;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class JobBuilder {

    private final Job.JobBuilder jobBuilder = Job.builder();

    private final SimpleDateFormat workspaceDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public JobBuilder job(String jobName, String jobColor, String jobUrl, boolean inQueue, boolean buildable) {
        jobBuilder.name(jobName).color(jobColor).url(jobUrl).inQueue(inQueue).buildable(buildable);
        jobBuilder.displayName(jobName);
        jobBuilder.fullName(jobName);
        //jobBuilder.fullDisplayName(jobName);
        return this;
    }

    public JobBuilder displayName(String displayName) {
        jobBuilder.displayName(displayName);
        return this;
    }

    public JobBuilder fullName(@NotNull String fullName) {
        jobBuilder.fullName(fullName);
        return this;
    }

    public JobBuilder fullDisplayName(@NotNull String fullDisplayName) {
        jobBuilder.fullDisplayName(fullDisplayName);
        return this;
    }

    public JobBuilder lastBuild(String buildUrl, int number, String status, boolean isBuilding, String buildingDate, Long timestamp, Long duration) {
        return lastBuild(Build.createBuildFromWorkspace(buildUrl, number, status, isBuilding, buildingDate, timestamp, duration, workspaceDateFormat));
    }

    public JobBuilder lastBuild(Build lastBuild) {
        jobBuilder.lastBuild(lastBuild);
        return this;
    }

    public JobBuilder health(String healthLevel, String healthDescription) {
        jobBuilder.health(new Job.Health(healthLevel, healthDescription));
        return this;
    }

    public JobBuilder parameter(String paramName, String paramType, String defaultValue, String... choices) {
        final JobParameter parameter = JobParameter.builder()
                .name(paramName)
                .jobParameterType(JobParameterType.getType(paramType, null))
                .defaultValue(defaultValue)
                .choices(Arrays.asList(choices))
                .build();
        jobBuilder.parameter(parameter);
        return this;
    }

    public Job get() {
        return jobBuilder.build();
    }

    public JobBuilder availableBuildTypes(EnumSet<BuildType> buildTypes) {
         jobBuilder.availableBuildTypes(buildTypes);
         return this;
    }
}
