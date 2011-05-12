package org.codinjutsu.tools.jenkins.model;

import java.util.LinkedList;
import java.util.List;

public class Jenkins {

    private final String name;

    private List<View> views;
    private View primaryView;

    private List<Job> jobs;


    public Jenkins(String description) {
        this.name = description;
        this.jobs = new LinkedList<Job>();
        this.views = new LinkedList<View>();
    }


    public List<Job> getJobs() {
        return jobs;
    }


    public List<View> getViews() {
        return views;
    }


    public String getName() {
        return name;
    }


    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }


    public void setViews(List<View> views) {
        this.views = views;
    }


    public void setPrimaryView(View primaryView) {
        this.primaryView = primaryView;
    }


    public View getPrimaryView() {
        return primaryView;
    }
}