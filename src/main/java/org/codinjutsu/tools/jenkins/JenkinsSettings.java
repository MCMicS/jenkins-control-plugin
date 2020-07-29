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

package org.codinjutsu.tools.jenkins;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import lombok.*;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.security.JenkinsVersion;
import org.codinjutsu.tools.jenkins.util.JobUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

@State(
        name = "Jenkins.Settings",
        storages = {
                @Storage(StoragePathMacros.WORKSPACE_FILE)
        }
)
public class JenkinsSettings implements PersistentStateComponent<JenkinsSettings.State> {

    public static final String JENKINS_SETTINGS_PASSWORD_KEY = "JENKINS_SETTINGS_PASSWORD_KEY";
    private State myState = new State();

    public static JenkinsSettings getSafeInstance(Project project) {
        JenkinsSettings settings = ServiceManager.getService(project, JenkinsSettings.class);
        return settings != null ? settings : new JenkinsSettings();
    }

    @Override
    public State getState() {
//        ensurePasswordIsStored();
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }

    public String getUsername() {
        return myState.getUsername();
    }

    public void setUsername(String username) {
        myState.setUsername(username);
    }

    public String getCrumbData() {
        return myState.getCrumbData();
    }

    public void setCrumbData(String crumbData) {
        myState.setCrumbData(crumbData);
    }

    public String getPassword() {
        String password = PasswordSafe.getInstance().getPassword(getPasswordCredentialAttributes());
        return StringUtils.defaultIfEmpty(password, "");
    }

    public void setPassword(String password) {
        PasswordSafe.getInstance().setPassword(getPasswordCredentialAttributes(), StringUtils.isNotBlank(password) ? password : "");
    }

    @NotNull
    private CredentialAttributes getPasswordCredentialAttributes() {
        return new CredentialAttributes(JenkinsAppSettings.class.getName(), JENKINS_SETTINGS_PASSWORD_KEY,
                JenkinsAppSettings.class);
    }

    public void addFavorite(@NotNull List<Job> jobs) {
        jobs.stream().map(JobUtil::createFavoriteJob).forEach(myState::addFavoriteJobs);
    }

    public boolean isFavoriteJob(@NotNull Job job) {
        return myState.getFavoriteJobs().stream().anyMatch(favoriteJob -> JobUtil.isFavoriteJob(job, favoriteJob));
    }

    public void removeFavorite(@NotNull List<Job> selectedJobs) {
        selectedJobs.forEach(jobToRemove -> myState.removeFavoriteJob(
                favoriteJob -> JobUtil.isFavoriteJob(jobToRemove, favoriteJob))
        );
    }

    @NotNull
    public List<FavoriteJob> getFavoriteJobs() {
        return myState.getFavoriteJobs();
    }

    public boolean isFavoriteViewEmpty() {
        return myState.getFavoriteJobs().isEmpty();
    }

    public String getLastSelectedView() {
        return myState.getLastSelectedView();
    }

    public void setLastSelectedView(String viewName) {
        myState.setLastSelectedView(viewName);
    }

    public boolean isSecurityMode() {
        return StringUtils.isNotBlank(getUsername());
    }

    public JenkinsVersion getVersion() {
        return this.myState.getJenkinsVersion();
    }

    public void setVersion(JenkinsVersion jenkinsVersion) {
        this.myState.setJenkinsVersion(jenkinsVersion);
    }

    public void clearFavoriteJobs() {
        myState.clearFavoriteJobs();
    }

    public boolean hasFavoriteJobs() {
        return !myState.getFavoriteJobs().isEmpty();
    }

    public int getConnectionTimeout() {
        return myState.getConnectionTimeout();
    }

    public void setConnectionTimeout(int timeoutInSeconds) {
        myState.setConnectionTimeout(timeoutInSeconds);
    }

    @Data
    public static class State {
        public static final String RESET_STR_VALUE = "";

        private static final int DEFAULT_CONNECTION_TIMEOUT = 10;

        private String username = RESET_STR_VALUE;

        private String crumbData = RESET_STR_VALUE;

        private String lastSelectedView;

        private List<FavoriteJob> favoriteJobs = new LinkedList<>();

        private JenkinsVersion jenkinsVersion = JenkinsVersion.VERSION_1;

        private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

        public void clearFavoriteJobs() {
            favoriteJobs.clear();
        }

        public void addFavoriteJobs(FavoriteJob favoriteJob) {
            favoriteJobs.add(favoriteJob);
        }

        public void removeFavoriteJob(Predicate<? super FavoriteJob> filter) {
            favoriteJobs.removeIf(filter);
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Tag("favorite")
    public static class FavoriteJob {

        @Setter(value = AccessLevel.NONE)
        @Attribute("name")
        private String name;

        @Setter(value = AccessLevel.NONE)
        @Attribute("url")
        private String url;
    }
}
