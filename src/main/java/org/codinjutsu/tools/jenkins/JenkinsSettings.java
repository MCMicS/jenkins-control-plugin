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
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.Job;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@State(
        name = "Jenkins.Settings",
        storages = {@Storage(id = "JenkinsSettings", file = "$WORKSPACE_FILE$")}
)
public class JenkinsSettings implements PersistentStateComponent<JenkinsSettings.State> {

    private static final Logger LOG = Logger.getInstance(JenkinsSettings.class.getName());

    public static final String JENKINS_SETTINGS_PASSWORD_KEY = "JENKINS_SETTINGS_PASSWORD_KEY";

    private boolean passwordChanged;
    private boolean masterPasswordRefused;

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
    public void loadState(State state) {
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
        String password;
        final PasswordSafeImpl passwordSafe = (PasswordSafeImpl) PasswordSafe.getInstance();
        try {
            password = passwordSafe.getMemoryProvider().getPassword(null, JenkinsAppSettings.class, JENKINS_SETTINGS_PASSWORD_KEY);
            if (password != null) {
                return password;
            }
            final MasterKeyPasswordSafe masterKeyProvider = passwordSafe.getMasterKeyProvider();
            if (!masterKeyProvider.isEmpty()) {
                // workaround for: don't ask for master password, if the requested password is not there.
                // this should be fixed in PasswordSafe: don't ask master password to look for keys
                // until then we assume that is PasswordSafe was used (there is anything there), then it makes sense to look there.
                password = masterKeyProvider.getPassword(null, JenkinsAppSettings.class, JENKINS_SETTINGS_PASSWORD_KEY);
            }
        } catch (PasswordSafeException e) {
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
            PasswordSafe.getInstance().storePassword(null, JenkinsAppSettings.class, JENKINS_SETTINGS_PASSWORD_KEY, StringUtils.isNotBlank(password) ? password : "");
        } catch (PasswordSafeException e) {
            LOG.info("Couldn't get password for key [" + JENKINS_SETTINGS_PASSWORD_KEY + "]", e);
        }
    }

    public void ensurePasswordIsStored() {
        try {
            if (passwordChanged && !masterPasswordRefused) {
                PasswordSafe.getInstance().storePassword(null, JenkinsAppSettings.class, JENKINS_SETTINGS_PASSWORD_KEY, getPassword());
            }
        } catch (MasterPasswordUnavailableException e) {
            LOG.info("Couldn't store password for key [" + JENKINS_SETTINGS_PASSWORD_KEY + "]", e);
            this.masterPasswordRefused = true;
        } catch (Exception e) {
            Messages.showErrorDialog("Error happened while storing password for github", "Error");
            LOG.info("Couldn't get password for key [" + JENKINS_SETTINGS_PASSWORD_KEY + "]", e);
        }
        this.passwordChanged = false;
    }

    public void addFavorite(Job job) {
        FavoriteJob favoriteJob = new FavoriteJob();
        favoriteJob.name = job.getName();
        favoriteJob.url = job.getUrl();
        myState.favoriteJobs.add(favoriteJob);
    }

    public boolean isAFavoriteJob(String jobName) {
        for (FavoriteJob favoriteJob : myState.favoriteJobs) {
            if (StringUtils.equals(jobName, favoriteJob.name)) {
                return true;
            }
        }
        return false;
    }

    public void removeFavorite(Job selectedJob) {
        for (Iterator<FavoriteJob> iterator = myState.favoriteJobs.iterator(); iterator.hasNext(); ) {
            FavoriteJob favoriteJob = iterator.next();
            if (StringUtils.equals(selectedJob.getName(), favoriteJob.name)) {
                iterator.remove();
            }
        }
    }

    public List<FavoriteJob> getFavoriteJobs() {
        return myState.favoriteJobs;
    }

    public boolean isFavoriteViewEmpty() {
        return myState.favoriteJobs.isEmpty();
    }

    public void setLastSelectedView(String viewName) {
        myState.lastSelectedView = viewName;
    }

    public String getLastSelectedView() {
        return myState.lastSelectedView;
    }

    public boolean isSecurityMode() {
            return StringUtils.isNotBlank(getUsername());
    }


    public static class State {

        public static final String RESET_STR_VALUE = "";

        public String username = RESET_STR_VALUE;

        public String crumbData = RESET_STR_VALUE;

        public String lastSelectedView;

        public List<FavoriteJob> favoriteJobs = new LinkedList<FavoriteJob>();

    }

    @Tag("favorite")
    public static class FavoriteJob {

        @Attribute("name")
        public String name;

        @Attribute("url")
        public String url;
    }
}
