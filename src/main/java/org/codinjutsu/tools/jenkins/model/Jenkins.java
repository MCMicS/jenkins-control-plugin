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

package org.codinjutsu.tools.jenkins.model;

import java.util.LinkedList;
import java.util.List;

public class Jenkins {

    private final String name;

    private List<Job> jobs;
    private List<View> views;

    private View primaryView;
    private String serverUrl;


    public Jenkins(String description, String serverUrl) {
        this.name = description;
        this.serverUrl = serverUrl;
        this.jobs = new LinkedList<Job>();
        this.views = new LinkedList<View>();
    }


    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }


    public List<Job> getJobs() {
        return jobs;
    }


    public List<Job> getJobList() {
        return jobs;
    }


    public List<View> getViews() {
        return views;
    }


    public String getName() {
        return name;
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

    public String getServerUrl() {
        return serverUrl;
    }

    public int getTotalBrokenBuilds() {
        int result = 0;
        for (Job job : jobs) {
            if (job.isBroken()) {
                result++;
            }
        }
        return result;
    }
}