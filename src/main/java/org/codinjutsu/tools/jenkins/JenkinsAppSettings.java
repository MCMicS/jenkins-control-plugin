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

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;

@State(
        name = "Jenkins.Application.Settings",
        storages = {
                @Storage(id = "JenkinsAppSettings", file = "$PROJECT_FILE$"),
                @Storage(file = "$PROJECT_CONFIG_DIR$/jenkinsSettings.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class JenkinsAppSettings implements PersistentStateComponent<JenkinsAppSettings.State> {

    public static final String DUMMY_JENKINS_SERVER_URL = "http://dummyjenkinsserver";
    public static final int DEFAULT_BUILD_DELAY = 0;
    public static final int RESET_PERIOD_VALUE = 0;

    private State myState = new State();

    public static JenkinsAppSettings getSafeInstance(Project project) {
        JenkinsAppSettings settings = ServiceManager.getService(project, JenkinsAppSettings.class);
        return settings != null ? settings : new JenkinsAppSettings();
    }

    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }


    public String getServerUrl() {
        return myState.serverUrl;
    }


    public void setServerUrl(String serverUrl) {
        myState.serverUrl = serverUrl;
    }


    public boolean isServerUrlSet() {
        return StringUtils.isNotEmpty(myState.serverUrl) && !DUMMY_JENKINS_SERVER_URL.equals(myState.serverUrl);
    }


    public int getBuildDelay() {
        return myState.delay;
    }


    public void setDelay(int delay) {
        myState.delay = delay;
    }


    public int getJobRefreshPeriod() {
        return myState.jobRefreshPeriod;
    }


    public void setJobRefreshPeriod(int jobRefreshPeriod) {
        myState.jobRefreshPeriod = jobRefreshPeriod;
    }


    public int getRssRefreshPeriod() {
        return myState.rssRefreshPeriod;
    }

    public void setRssRefreshPeriod(int rssRefreshPeriod) {
        myState.rssRefreshPeriod = rssRefreshPeriod;
    }

    public String getSuffix() {
        return myState.suffix;
    }

    public void setSuffix(String suffix) {
        myState.suffix = suffix;
    }

    public boolean shouldDisplaySuccessOrStable() {
        return myState.rssSettings.displaySuccessOrStable;
    }

    public boolean shouldDisplayFailOrUnstable() {
        return myState.rssSettings.displayUnstableOrFail;
    }

    public boolean shouldDisplayAborted() {
        return myState.rssSettings.displayAborted;
    }

    public void setIgnoreSuccessOrStable(boolean ignoreSucessOrStable) {
        myState.rssSettings.displaySuccessOrStable = ignoreSucessOrStable;
    }

    public void setDisplayUnstableOrFail(boolean displayUnstableOrFail) {
        myState.rssSettings.displayUnstableOrFail = displayUnstableOrFail;
    }

    public void setDisplayAborted(boolean displayAborted) {
        myState.rssSettings.displayAborted = displayAborted;
    }

    public boolean shouldDisplayOnLogEvent(Build build) {
        BuildStatusEnum buildStatus = build.getStatus();
        if (BuildStatusEnum.SUCCESS.equals(buildStatus) || BuildStatusEnum.STABLE.equals(buildStatus)) {
            return shouldDisplaySuccessOrStable();
        }
        if (BuildStatusEnum.FAILURE.equals(buildStatus) || BuildStatusEnum.UNSTABLE.equals(buildStatus)) {
            return shouldDisplayFailOrUnstable();
        }
        if (BuildStatusEnum.ABORTED.equals(buildStatus)) {
            return shouldDisplayAborted();
        }

        return false;
    }

    public static class State {

        public String serverUrl = DUMMY_JENKINS_SERVER_URL;
        public int delay = DEFAULT_BUILD_DELAY;
        public int jobRefreshPeriod = RESET_PERIOD_VALUE;
        public int rssRefreshPeriod = RESET_PERIOD_VALUE;
        public String suffix = "";

        public RssSettings rssSettings = new RssSettings();
    }

    public static class RssSettings {
        public boolean displaySuccessOrStable = true;
        public boolean displayUnstableOrFail = true;
        public boolean displayAborted = true;
    }
}
