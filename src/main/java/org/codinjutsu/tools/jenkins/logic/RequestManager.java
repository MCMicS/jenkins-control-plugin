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

package org.codinjutsu.tools.jenkins.logic;

import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.codinjutsu.tools.jenkins.security.SecurityClientFactory;
import org.codinjutsu.tools.jenkins.security.SecurityMode;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RequestManager {

    private static final String BUILDHIVE_CLOUDBEES = "buildhive";

    private UrlBuilder urlBuilder;
    private SecurityClient securityClient;

    private JenkinsPlateform jenkinsPlateform = JenkinsPlateform.CLASSIC;

    private RssParser rssParser = new RssParser();

    private JenkinsJsonParser jsonParser = new JenkinsJsonParser();


    public RequestManager(SecurityClient securityClient) {
        this.urlBuilder = new UrlBuilder();
        this.securityClient = securityClient;
    }

    public RequestManager(String crumbFile) {
        this(SecurityClientFactory.none(crumbFile));
    }

    public Jenkins loadJenkinsWorkspace(JenkinsConfiguration configuration) {
        URL url = urlBuilder.createJenkinsWorkspaceUrl(configuration);
        String jenkinsWorkspaceData = securityClient.execute(url);

        if (configuration.getServerUrl().contains(BUILDHIVE_CLOUDBEES)) {//TODO hack need to refactor
            jenkinsPlateform = JenkinsPlateform.CLOUDBEES;
        } else {
            jenkinsPlateform = JenkinsPlateform.CLASSIC;
        }

        Jenkins jenkins = jsonParser.createWorkspace(jenkinsWorkspaceData, configuration.getServerUrl());

        int jenkinsPort = url.getPort();
        URL viewUrl = urlBuilder.createViewUrl(jenkinsPlateform, jenkins.getPrimaryView().getUrl());
        int viewPort = viewUrl.getPort();

        if (isJenkinsPortSet(jenkinsPort) && jenkinsPort != viewPort) {
            throw new ConfigurationException(String.format("Jenkins Port seems to be incorrect in the Server configuration page. Please fix 'Jenkins URL' at %s/configure", configuration.getServerUrl()));
        }

        return jenkins;
    }

    private boolean isJenkinsPortSet(int jenkinsPort) {
        return jenkinsPort != -1;
    }

    public Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsConfiguration configuration) {
        URL url = urlBuilder.createRssLatestUrl(configuration.getServerUrl());

        String rssData = securityClient.execute(url);

        return rssParser.loadJenkinsRssLatestBuilds(rssData);
    }

    public List<Job> loadJenkinsView(String viewUrl) {
        URL url = urlBuilder.createViewUrl(jenkinsPlateform, viewUrl);
        String jenkinsViewData = securityClient.execute(url);
        if (jenkinsPlateform.equals(JenkinsPlateform.CLASSIC)) {
            return jsonParser.createViewJobs(jenkinsViewData);
        } else {
            return jsonParser.createCloudbeesViewJobs(jenkinsViewData);
        }
    }

    public Job loadJob(String jenkinsJobUrl) {
        URL url = urlBuilder.createJobUrl(jenkinsJobUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createJob(jenkinsJobData);
    }

    public void runBuild(Job job, JenkinsConfiguration configuration) {
        URL url = urlBuilder.createRunJobUrl(job.getUrl(), configuration);
        securityClient.execute(url);
    }

    public void runParameterizedBuild(Job job, JenkinsConfiguration configuration, Map<String, String> paramValueMap) {
        URL url = urlBuilder.createRunParameterizedJobUrl(job.getUrl(), configuration, paramValueMap);
        securityClient.execute(url);
    }

    public void authenticate(String serverUrl, SecurityMode securityMode, String username, String password, String crumbData) {
        securityClient = SecurityClientFactory.create(securityMode, username, password, crumbData);
        securityClient.connect(urlBuilder.createAuthenticationUrl(serverUrl));
    }

    public List<Job> loadFavoriteJobs(List<JenkinsConfiguration.FavoriteJob> favoriteJobs) {
        List<Job> jobs = new LinkedList<Job>();
        for (JenkinsConfiguration.FavoriteJob favoriteJob : favoriteJobs) {
            jobs.add(loadJob(favoriteJob.url));
        }
        return jobs;
    }
}
