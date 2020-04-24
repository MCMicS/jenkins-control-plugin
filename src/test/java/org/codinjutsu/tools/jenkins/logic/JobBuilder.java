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

import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.JobParameterType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class JobBuilder {

    private final Job.JobBuilder jobBuilder = Job.builder();

    public JobBuilder job(String jobName, String jobColor, String jobUrl, boolean inQueue, boolean buildable) {
        jobBuilder.name(jobName).color(jobColor).url(jobUrl).inQueue(inQueue).buildable(buildable);
        jobBuilder.displayName(jobName);
        jobBuilder.fullName(jobName);
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

    public JobBuilder lastBuild(String buildUrl, String number, String status, boolean isBuilding, String buildingDate, Long timestamp, Long duration) {
        return lastBuild(Build.createBuildFromWorkspace(buildUrl, number, status, isBuilding, buildingDate, timestamp, duration));
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
}
