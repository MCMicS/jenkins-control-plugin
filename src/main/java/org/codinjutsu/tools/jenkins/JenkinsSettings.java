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

@State(
        name = "Jenkins.Settings",
        storages = {
                @Storage(StoragePathMacros.WORKSPACE_FILE)
        }
)
public class JenkinsSettings implements PersistentStateComponent<JenkinsSettings.State> {

    public static final String JENKINS_SETTINGS_PASSWORD_KEY = "JENKINS_SETTINGS_PASSWORD_KEY";
    public static final String PLUGIN_ID = "Jenkins Control Plugin";
    private static final Logger LOG = Logger.getInstance(JenkinsSettings.class.getName());
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
        return myState.username;
    }

    public void setUsername(String username) {
        myState.username = username;
    }

    public String getCrumbData() {
        return myState.crumbData;
    }

    public void setCrumbData(String crumbData) {
        myState.crumbData = crumbData;
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
        jobs.stream().map(JobUtil::createFavoriteJob).forEach(myState.favoriteJobs::add);
    }

    public boolean isFavoriteJob(@NotNull Job job) {
        return myState.favoriteJobs.stream().anyMatch(favoriteJob -> JobUtil.isFavoriteJob(job, favoriteJob));
    }

    public void removeFavorite(@NotNull List<Job> selectedJobs) {
        selectedJobs.forEach(jobToRemove -> myState.favoriteJobs.removeIf(
                favoriteJob -> JobUtil.isFavoriteJob(jobToRemove, favoriteJob))
        );
    }

    @NotNull
    public List<FavoriteJob> getFavoriteJobs() {
        return myState.favoriteJobs;
    }

    public boolean isFavoriteViewEmpty() {
        return myState.favoriteJobs.isEmpty();
    }

    public String getLastSelectedView() {
        return myState.lastSelectedView;
    }

    public void setLastSelectedView(String viewName) {
        myState.lastSelectedView = viewName;
    }

    public boolean isSecurityMode() {
        return StringUtils.isNotBlank(getUsername());
    }

    public JenkinsVersion getVersion() {
        return this.myState.jenkinsVersion;
    }

    public void setVersion(JenkinsVersion jenkinsVersion) {
        this.myState.jenkinsVersion = jenkinsVersion;
    }

    public void clearFavoriteJobs() {
        myState.favoriteJobs.clear();
    }

    public boolean hasFavoriteJobs() {
        return !myState.favoriteJobs.isEmpty();
    }

    public static class State {

        public static final String RESET_STR_VALUE = "";

        public String username = RESET_STR_VALUE;

        public String crumbData = RESET_STR_VALUE;

        public String lastSelectedView;

        public List<FavoriteJob> favoriteJobs = new LinkedList<>();

        public JenkinsVersion jenkinsVersion = JenkinsVersion.VERSION_1;

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
