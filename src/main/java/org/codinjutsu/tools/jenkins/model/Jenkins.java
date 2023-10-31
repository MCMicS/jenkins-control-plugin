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

import com.intellij.openapi.util.text.StringUtil;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class Jenkins {

    private @Nullable String name;
    private String serverUrl;

    private List<Job> jobs;

    private List<View> views;
    private View primaryView;

    public Jenkins(@Nullable String description, String serverUrl) {
        this.name = description;
        this.serverUrl = serverUrl;
        this.jobs = new LinkedList<>();
        this.views = new LinkedList<>();
    }


    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }


    public List<Job> getJobs() {
        return jobs;
    }


    public List<View> getViews() {
        return views;
    }


    public @Nullable String getName() {
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

    public View getViewByName(String lastSelectedViewName) {
        for (View view : views) {
            if (StringUtil.equals(lastSelectedViewName, view.getName())) {
                return view;
            }
        }

        return null;
    }

    public void update(Jenkins jenkins) {
        this.name = jenkins.getName();
        this.serverUrl = jenkins.getServerUrl();
        this.jobs.clear();
        this.jobs.addAll(jenkins.getJobs());
        this.views.clear();
        this.views.addAll(jenkins.getViews());
        this.primaryView = jenkins.getPrimaryView();
    }

    public static Jenkins byDefault() {
        return new Jenkins("", JenkinsAppSettings.DUMMY_JENKINS_SERVER_URL);
    }

    public String getNameToRender() {
        final var description = getName();
        final var label = new StringBuilder("Jenkins");
        if (StringUtil.isNotEmpty(description)) {
            label.append(' ');
            label.append(description);
        }
        return label.toString();
    }
}
