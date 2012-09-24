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

package org.codinjutsu.tools.jenkins;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.security.SecurityMode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class JenkinsConfiguration {

    public static final String DUMMY_JENKINS_SERVER_URL = "http://dummyjenkinsserver";
    public static final int DEFAULT_BUILD_DELAY = 0;
    public static final int RESET_PERIOD_VALUE = 0;

    public static final String RESET_STR_VALUE = "";

    private String serverUrl = DUMMY_JENKINS_SERVER_URL;

    private int delay = DEFAULT_BUILD_DELAY;

    private SecurityMode securityMode = SecurityMode.NONE;

    private String username = RESET_STR_VALUE;
    private String passwordFile = RESET_STR_VALUE;

    private String crumbFile = RESET_STR_VALUE;

    private int jobRefreshPeriod = RESET_PERIOD_VALUE;
    private int rssRefreshPeriod = RESET_PERIOD_VALUE;

    private boolean enableJobAutoRefresh = false;
    private boolean enableRssAutoRefresh = false;

    public List<FavoriteJob> favoriteJobs = new LinkedList<FavoriteJob>();

    private String lastSelectedView;

    public String getServerUrl() {
        return serverUrl;
    }


    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    public boolean isServerUrlSet() {
        return StringUtils.isNotEmpty(serverUrl) && !JenkinsConfiguration.DUMMY_JENKINS_SERVER_URL.equals(serverUrl);
    }


    public int getBuildDelay() {
        return delay;
    }


    public void setDelay(int delay) {
        this.delay = delay;
    }


    public int getJobRefreshPeriod() {
        return jobRefreshPeriod;
    }


    public void setJobRefreshPeriod(int jobRefreshPeriod) {
        this.jobRefreshPeriod = jobRefreshPeriod;
    }


    public int getRssRefreshPeriod() {
        return rssRefreshPeriod;
    }


    public void setRssRefreshPeriod(int rssRefreshPeriod) {
        this.rssRefreshPeriod = rssRefreshPeriod;
    }


    public boolean isEnableJobAutoRefresh() {
        return enableJobAutoRefresh;
    }


    public void setEnableJobAutoRefresh(boolean enableJobAutoRefresh) {
        this.enableJobAutoRefresh = enableJobAutoRefresh;
    }


    public boolean isEnableRssAutoRefresh() {
        return enableRssAutoRefresh;
    }


    public void setEnableRssAutoRefresh(boolean enableRssAutoRefresh) {
        this.enableRssAutoRefresh = enableRssAutoRefresh;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordFile() {
        return passwordFile;
    }

    public void setPasswordFile(String passwordFile) {
        this.passwordFile = passwordFile;
    }

    public void setSecurityMode(SecurityMode SecurityMode) {
        this.securityMode = SecurityMode;
    }

    public SecurityMode getSecurityMode() {
        return securityMode;
    }

    public String getCrumbFile() {
        return crumbFile;
    }

    public void setCrumbFile(String crumbFile) {
        this.crumbFile = crumbFile;
    }

    public void setLastSelectedView(String viewName) {
        this.lastSelectedView = viewName;
    }

    public String getLastSelectedView() {
        return lastSelectedView;
    }

    public void addFavorite(Job job) {
        FavoriteJob favoriteJob = new FavoriteJob();
        favoriteJob.name = job.getName();
        favoriteJob.url = job.getUrl();
        favoriteJobs.add(favoriteJob);
    }

    public boolean isAFavoriteJob(String jobName) {
        for (FavoriteJob favoriteJob : favoriteJobs) {
            if (StringUtils.equals(jobName, favoriteJob.name)) {
                return true;
            }
        }
        return false;
    }

    public void removeFavorite(Job selectedJob) {
        for (Iterator<FavoriteJob> iterator = favoriteJobs.iterator(); iterator.hasNext(); ) {
            FavoriteJob favoriteJob = iterator.next();
            if (StringUtils.equals(selectedJob.getName(), favoriteJob.name)) {
                iterator.remove();
            }
        }
    }

    public List<FavoriteJob> getFavoriteJobs() {
        return favoriteJobs;
    }

    public boolean isFavoriteViewEmpty() {
        return favoriteJobs.isEmpty();
    }

    @Tag("favorite")
    public static class FavoriteJob {

        @Attribute("name")
        public String name;

        @Attribute("url")
        public String url;
    }
}
