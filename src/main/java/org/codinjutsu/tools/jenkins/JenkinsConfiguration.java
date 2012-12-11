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

import com.intellij.ide.passwordSafe.MasterPasswordUnavailableException;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.ide.passwordSafe.impl.PasswordSafeImpl;
import com.intellij.ide.passwordSafe.impl.providers.masterKey.MasterKeyPasswordSafe;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.security.SecurityMode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class JenkinsConfiguration {

    private static final Logger LOG = Logger.getInstance(JenkinsConfiguration.class.getName());

    public static final String JENKINS_SETTINGS_PASSWORD_KEY = "JENKINS_SETTINGS_PASSWORD_KEY";

    public static final String DUMMY_JENKINS_SERVER_URL = "http://dummyjenkinsserver";
    public static final int DEFAULT_BUILD_DELAY = 0;
    public static final int RESET_PERIOD_VALUE = 0;

    public static final String RESET_STR_VALUE = "";

    private String serverUrl = DUMMY_JENKINS_SERVER_URL;

    private int delay = DEFAULT_BUILD_DELAY;

    private SecurityMode securityMode = SecurityMode.NONE;

    private String username = RESET_STR_VALUE;
    protected String password = RESET_STR_VALUE;

    private String crumbFile = RESET_STR_VALUE;

    private int jobRefreshPeriod = RESET_PERIOD_VALUE;
    private int rssRefreshPeriod = RESET_PERIOD_VALUE;

    private boolean enableJobAutoRefresh = false;
    private boolean enableRssAutoRefresh = false;

    public List<FavoriteJob> favoriteJobs = new LinkedList<FavoriteJob>();

    private String lastSelectedView;

    private boolean passwordChanged;
    private boolean masterPasswordRefused;

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

    public String getPassword() {
        String password;
        final Project project = ProjectManager.getInstance().getDefaultProject();
        final PasswordSafeImpl passwordSafe = (PasswordSafeImpl)PasswordSafe.getInstance();
        try {
            password = passwordSafe.getMemoryProvider().getPassword(project, JenkinsConfiguration.class, JENKINS_SETTINGS_PASSWORD_KEY);
            if (password != null) {
                return password;
            }
            final MasterKeyPasswordSafe masterKeyProvider = passwordSafe.getMasterKeyProvider();
            if (!masterKeyProvider.isEmpty()) {
                // workaround for: don't ask for master password, if the requested password is not there.
                // this should be fixed in PasswordSafe: don't ask master password to look for keys
                // until then we assume that is PasswordSafe was used (there is anything there), then it makes sense to look there.
                password = masterKeyProvider.getPassword(project, JenkinsConfiguration.class, JENKINS_SETTINGS_PASSWORD_KEY);
            }
        }
        catch (PasswordSafeException e) {
            LOG.info("Couldn't get password for key [" + JENKINS_SETTINGS_PASSWORD_KEY + "]", e);
            masterPasswordRefused = true;
            password = "";
        }

        passwordChanged = false;
        return password != null ? password : "";
    }

    public void setPassword(String password) {
            passwordChanged = !getPassword().equals(password);
            try {
                PasswordSafe.getInstance().storePassword(null, JenkinsConfiguration.class, JENKINS_SETTINGS_PASSWORD_KEY, StringUtils.isNotBlank(password) ? password : "");
            }
            catch (PasswordSafeException e) {
                LOG.info("Couldn't get password for key [" + JENKINS_SETTINGS_PASSWORD_KEY + "]", e);
            }
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

    public void ensurePasswordIsStored() {
        try {
            if (passwordChanged && !masterPasswordRefused) {
                PasswordSafe.getInstance().storePassword(null, JenkinsConfiguration.class, JENKINS_SETTINGS_PASSWORD_KEY, getPassword());
            }
        }
        catch (MasterPasswordUnavailableException e){
            LOG.info("Couldn't store password for key [" + JENKINS_SETTINGS_PASSWORD_KEY + "]", e);
            this.masterPasswordRefused = true;
        }
        catch (Exception e) {
            Messages.showErrorDialog("Error happened while storing password for github", "Error");
            LOG.info("Couldn't get password for key [" + JENKINS_SETTINGS_PASSWORD_KEY + "]", e);
        }
        this.passwordChanged = false;
    }

    @Tag("favorite")
    public static class FavoriteJob {

        @Attribute("name")
        public String name;

        @Attribute("url")
        public String url;
    }
}
