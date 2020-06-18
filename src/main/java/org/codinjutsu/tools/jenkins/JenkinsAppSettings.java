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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;

@State(
        name = "Jenkins.Application.Settings",
        storages = {
                @Storage("jenkinsSettings.xml")
        }
)
public class JenkinsAppSettings implements PersistentStateComponent<JenkinsAppSettings.State> {

    public static final String DUMMY_JENKINS_SERVER_URL = "http://dummyjenkinsserver";
    public static final int DEFAULT_BUILD_DELAY = 0;
    public static final int DEFAULT_BUILD_RETRY = 0;
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
        return myState.getServerUrl();
    }

    public void setServerUrl(String serverUrl) {
        myState.setServerUrl(serverUrl);
    }

    public boolean isServerUrlSet() {
        final String serverUrl = myState.getServerUrl();
        return StringUtils.isNotEmpty(serverUrl) && !DUMMY_JENKINS_SERVER_URL.equals(serverUrl);
    }

    public int getBuildDelay() {
        return myState.getDelay();
    }

    public void setDelay(int delay) {
        myState.setDelay(delay);
    }

    public int getJobRefreshPeriod() {
        return myState.getJobRefreshPeriod();
    }

    public void setJobRefreshPeriod(int jobRefreshPeriod) {
        myState.setJobRefreshPeriod(jobRefreshPeriod);
    }

    public int getRssRefreshPeriod() {
        return myState.getRssRefreshPeriod();
    }

    public void setRssRefreshPeriod(int rssRefreshPeriod) {
        myState.setRssRefreshPeriod(rssRefreshPeriod);
    }

    public String getSuffix() {
        return myState.getSuffix();
    }

    public void setSuffix(String suffix) {
        myState.setSuffix(suffix);
    }

    private RssSettings getRssSettings() {
        return myState.getRssSettings();
    }

    public boolean shouldDisplaySuccessOrStable() {
        return getRssSettings().isDisplaySuccessOrStable();
    }

    public boolean shouldDisplayFailOrUnstable() {
        return getRssSettings().isDisplayUnstableOrFail();
    }

    public boolean shouldDisplayAborted() {
        return getRssSettings().isDisplayAborted();
    }

    public void setIgnoreSuccessOrStable(boolean ignoreSucessOrStable) {
        getRssSettings().setDisplaySuccessOrStable(ignoreSucessOrStable);
    }

    public void setDisplayUnstableOrFail(boolean displayUnstableOrFail) {
        getRssSettings().setDisplayUnstableOrFail(displayUnstableOrFail);
    }

    public void setDisplayAborted(boolean displayAborted) {
        getRssSettings().setDisplayAborted(displayAborted);
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

    public int getNumBuildRetries() {
        return myState.getNumBuildRetries();
    }

    public void setNumBuildRetries(int numBuildRetries) {
        myState.setNumBuildRetries(numBuildRetries);
    }

    public boolean isUseGreenColor() {
        return myState.isUseGreenColor();
    }

    public void setUseGreenColor(boolean useGreenColor) {
        myState.setUseGreenColor(useGreenColor);
    }

    public boolean isShowAllInStatusbar() {
        return myState.isShowAllInStatusbar();
    }

    public void setShowAllInStatusbar(boolean showAllInStatusbar) {
        myState.setShowAllInStatusbar(showAllInStatusbar);
    }

    @Data
    public static class State {

        private String serverUrl = DUMMY_JENKINS_SERVER_URL;
        private int delay = DEFAULT_BUILD_DELAY;
        private int jobRefreshPeriod = RESET_PERIOD_VALUE;
        private int rssRefreshPeriod = RESET_PERIOD_VALUE;
        private String suffix = "";

        private int numBuildRetries = DEFAULT_BUILD_RETRY;
        private RssSettings rssSettings = new RssSettings();
        private boolean useGreenColor = false;
        private boolean showAllInStatusbar = false;
    }

    @Data
    public static class RssSettings {
        private boolean displaySuccessOrStable = true;
        private boolean displayUnstableOrFail = true;
        private boolean displayAborted = true;
    }
}
