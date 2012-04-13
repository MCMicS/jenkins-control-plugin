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

package org.codinjutsu.tools.jenkins.logic;

import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Job;

public class JobBuilder {

    private Job job;

    public JobBuilder job(String jobName, String jobColor, String jobUrl, String inQueue, String buildable) {
        job = Job.createJob(jobName, jobColor, jobUrl, inQueue, buildable);
        return this;
    }

    public JobBuilder lastBuild(String buildUrl, String number, String status, String isBuilding, String buildingDate) {
        job.setLastBuild(Build.createBuildFromWorkspace(buildUrl, number, status, isBuilding, buildingDate));
        return this;
    }

    public JobBuilder health(String healthLevel, String healthDescription) {
        job.setHealth(Job.Health.createHealth(healthLevel, healthDescription));
        return this;
    }

    public JobBuilder parameter(String paramName, String paramType, String defaultValue, String... choices) {
        job.addParameter(paramName, paramType, defaultValue, choices);
        return this;
    }

    public Job get() {
        return job;
    }

}
