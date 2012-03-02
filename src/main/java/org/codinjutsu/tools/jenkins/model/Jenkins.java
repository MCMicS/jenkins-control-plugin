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

import org.codinjutsu.tools.jenkins.logic.JenkinsBrowserLogic;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Jenkins {

    private final String name;

    private final Map<String, Job> jobs;

    private List<View> views;
    private View primaryView;


    public Jenkins(String description) {
        this.name = description;
        this.jobs = new LinkedHashMap<String, Job>();
        this.views = new LinkedList<View>();
    }


    public void addJobs(List<Job> jobsToAdd, boolean needToReset, JenkinsBrowserLogic.JobStatusCallback jobStatusCallback) {
        if (needToReset) {
            jobs.clear();
        }
        for (Job jobToAdd : jobsToAdd) {
            Job job = jobs.get(jobToAdd.getName());
            if (job != null) {
                boolean updated = job.updateContentWith(jobToAdd);
                if (updated) {
                    jobStatusCallback.notifyUpdatedStatus(jobToAdd);
                }
            } else {
                jobs.put(jobToAdd.getName(), jobToAdd);
            }
        }
    }


    public Map<String, Job> getJobs() {
        return jobs;
    }


    public List<Job> getJobList() {
        return new LinkedList<Job>(jobs.values());
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
}