package org.codinjutsu.tools.jenkins.logic;

import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Job;

public class JobBuilder {

    private Job job;

    public JobBuilder job(String jobName, String jobColor, String jobUrl, String inQueue) {
        job = Job.createJob(jobName, jobColor, jobUrl, inQueue);
        return this;
    }

    public JobBuilder lastBuild(String buildUrl, String number, String status, String isBuilding) {
        job.setLastBuild(Build.createBuild(buildUrl, number, status, isBuilding));
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
