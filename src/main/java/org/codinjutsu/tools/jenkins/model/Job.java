package org.codinjutsu.tools.jenkins.model;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Job {
    private final String name;
    private final String url;

    private String color;
    private boolean inQueue;
    
    private Build lastBuild;


    private Job(String name, String color, String url, Boolean inQueue) {
        this.name = name;
        this.color = color;
        this.url = url;
        this.inQueue = inQueue;
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


    @Override
    public boolean equals(Object obj) {
        return reflectionEquals(this, obj);
    }


    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }


    @Override
    public String toString() {
        return reflectionToString(this, SHORT_PREFIX_STYLE);
    }


    public static Job createJob(String jobName, String jobColor, String jobUrl, String inQueue) {
        return new Job(jobName, jobColor, jobUrl, Boolean.valueOf(inQueue));
    }

    public void updateContentWith(Job updatedJob) {
        this.color = updatedJob.getColor();
        this.inQueue = updatedJob.isInQueue();
        this.lastBuild = updatedJob.getLastBuild();
    }
}
