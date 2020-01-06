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

package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.TestChildReport;
import com.offbytwo.jenkins.model.TestResult;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.security.JenkinsVersion;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.codinjutsu.tools.jenkins.security.SecurityClientFactory;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class RequestManager implements RequestManagerInterface {

    private static final Logger logger = Logger.getLogger(RequestManager.class);

    private static final String BUILDHIVE_CLOUDBEES = "buildhive";

    private final UrlBuilder urlBuilder;

    private SecurityClient securityClient;

    private JenkinsPlateform jenkinsPlateform = JenkinsPlateform.CLASSIC;

    private RssParser rssParser = new RssParser();

    private JenkinsParser jsonParser = new JenkinsJsonParser();
    private JenkinsServer jenkinsServer;

    public static RequestManager getInstance(Project project) {
        return ServiceManager.getService(project, RequestManager.class);
    }

    public RequestManager(Project project) {
        this.urlBuilder = UrlBuilder.getInstance(project);
    }

    @Override
    public Jenkins loadJenkinsWorkspace(JenkinsAppSettings configuration) {
        if (handleNotYetLoggedInState()) return null;
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
            throw new ConfigurationException(String.format("Jenkins Server Port Mismatch: expected='%s' - actual='%s'. Look at the value of 'Jenkins URL' at %s/configure", jenkinsPort, viewPort, configuration.getServerUrl()));
        }

        if (!StringUtils.equals(url.getHost(), viewUrl.getHost())) {
            throw new ConfigurationException(String.format("Jenkins Server Host Mismatch: expected='%s' - actual='%s'. Look at the value of 'Jenkins URL' at %s/configure", url.getHost(), viewUrl.getHost(), configuration.getServerUrl()));
        }

        return jenkins;
    }

    private boolean isJenkinsPortSet(int jenkinsPort) {
        return jenkinsPort != -1;
    }

    /**
     * Note! needs to be called after plugin is logged in
     *
     * @param configuration
     * @return
     */
    @Override
    public Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsAppSettings configuration) {
        if (handleNotYetLoggedInState()) return Collections.emptyMap();
        URL url = urlBuilder.createRssLatestUrl(configuration.getServerUrl());

        String rssData = securityClient.execute(url);

        return rssParser.loadJenkinsRssLatestBuilds(rssData);
    }

    private List<Job> loadJenkinsView(String viewUrl) {
        if (handleNotYetLoggedInState()) return Collections.emptyList();
        URL url = urlBuilder.createViewUrl(jenkinsPlateform, viewUrl);
        String jenkinsViewData = securityClient.execute(url);
        if (jenkinsPlateform.equals(JenkinsPlateform.CLASSIC)) {
            return jsonParser.createViewJobs(jenkinsViewData);
        } else {
            return jsonParser.createCloudbeesViewJobs(jenkinsViewData);
        }
    }

    private boolean handleNotYetLoggedInState() {
        boolean threadStack = false;
        boolean result = false;
        if (SwingUtilities.isEventDispatchThread()) {
            logger.warn("RequestManager.handleNotYetLoggedInState called from EDT");
            threadStack = true;
        }
        if (securityClient == null) {
            logger.warn("Not yet logged in, all calls until login will fail");
            threadStack = true;
            result = true;
        }
        if (threadStack)
            Thread.dumpStack();
        return result;
    }

    private Job loadJob(String jenkinsJobUrl) {
        if (handleNotYetLoggedInState()) return null;
        URL url = urlBuilder.createJobUrl(jenkinsJobUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createJob(jenkinsJobData);
    }

    private void stopBuild(String jenkinsBuildUrl) {
        if (handleNotYetLoggedInState()) return;
        URL url = urlBuilder.createStopBuildUrl(jenkinsBuildUrl);
        securityClient.execute(url);
    }

    private Build loadBuild(String jenkinsBuildUrl) {
        if (handleNotYetLoggedInState()) return null;
        URL url = urlBuilder.createBuildUrl(jenkinsBuildUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createBuild(jenkinsJobData);
    }

    private List<Build> loadBuilds(String jenkinsBuildUrl) {
        if (handleNotYetLoggedInState()) return null;
        URL url = urlBuilder.createBuildsUrl(jenkinsBuildUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createBuilds(jenkinsJobData);
    }

    @Override
    public void runBuild(Job job, JenkinsAppSettings configuration, Map<String, VirtualFile> files) {
        if (handleNotYetLoggedInState()) return;
        if (job.hasParameters()) {
            if (files.size() > 0) {
                for (String key : files.keySet()) {
                    if (!job.hasParameter(key)) {
                        files.remove(files.get(key));
                    }
                }
                securityClient.setFiles(files);
            }
        }
        runBuild(job, configuration);
    }

    @Override
    public void runBuild(Job job, JenkinsAppSettings configuration) {
        if (handleNotYetLoggedInState()) return;
        URL url = urlBuilder.createRunJobUrl(job.getUrl(), configuration);
        securityClient.execute(url);
    }

    @Override
    public void runParameterizedBuild(Job job, JenkinsAppSettings configuration, Map<String, String> paramValueMap) {
        if (handleNotYetLoggedInState()) return;
        URL url = urlBuilder.createRunParameterizedJobUrl(job.getUrl(), configuration, paramValueMap);
        securityClient.execute(url);
    }

    @Override
    public void authenticate(JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings) {
        SecurityClientFactory.setVersion(jenkinsSettings.getVersion());
        if (jenkinsSettings.isSecurityMode()) {
            securityClient = SecurityClientFactory.basic(jenkinsSettings.getUsername(), jenkinsSettings.getPassword(), jenkinsSettings.getCrumbData());
        } else {
            securityClient = SecurityClientFactory.none(jenkinsSettings.getCrumbData());
        }
        securityClient.connect(urlBuilder.createAuthenticationUrl(jenkinsAppSettings.getServerUrl()));

        jenkinsServer = new JenkinsServer(urlBuilder.createServerUrl(jenkinsAppSettings.getServerUrl()), jenkinsSettings.getUsername(), jenkinsSettings.getPassword());
    }

    @Override
    public void authenticate(String serverUrl, String username, String password, String crumbData, JenkinsVersion version) {
        SecurityClientFactory.setVersion(version);
        if (StringUtils.isNotBlank(username)) {
            securityClient = SecurityClientFactory.basic(username, password, crumbData);
        } else {
            securityClient = SecurityClientFactory.none(crumbData);
        }
        securityClient.connect(urlBuilder.createAuthenticationUrl(serverUrl));
    }

    @Override
    public List<Job> loadFavoriteJobs(List<JenkinsSettings.FavoriteJob> favoriteJobs) {
        if (handleNotYetLoggedInState()) return Collections.emptyList();
        List<Job> jobs = new LinkedList<>();
        for (JenkinsSettings.FavoriteJob favoriteJob : favoriteJobs) {
            jobs.add(loadJob(favoriteJob.url));
        }
        return jobs;
    }

    @Override
    public void stopBuild(Build build) {
        stopBuild(build.getUrl());
    }

    @Override
    public Job loadJob(Job job) {
        return loadJob(job.getUrl());
    }

    @Override
    public List<Job> loadJenkinsView(View view) {
        return loadJenkinsView(view.getUrl());
    }

    @Override
    public List<Build> loadBuilds(Job job) {
        return loadBuilds(job.getUrl());
    }

    @Override
    public Build loadBuild(Build build) {
        return loadBuild(build.getUrl());
    }

    @Override
    public String loadConsoleTextFor(Job job) {
        try {
            return jenkinsServer.getJob(job.getName()).getLastCompletedBuild().details().getConsoleOutputText();
        } catch (IOException e) {
            logger.warn("cannot load log for " + job.getName());
            return null;
        }
    }

    @Override
    public List<TestResult> loadTestResultsFor(Job job) {
        try {
            List<TestResult> result = new ArrayList<>();
            com.offbytwo.jenkins.model.Build lastCompletedBuild = jenkinsServer.getJob(job.getName()).getLastCompletedBuild();
            if (lastCompletedBuild.getTestResult() != null) {
                result.add(lastCompletedBuild.getTestResult());
            }
            if (lastCompletedBuild.getTestReport().getChildReports() != null) {
                result.addAll(lastCompletedBuild.getTestReport().getChildReports().stream()
                        .map(TestChildReport::getResult)
                        .collect(Collectors.toList()));
            }
            return result;
        } catch (IOException e) {
            logger.warn("cannot load test results for " + job.getName());
            return Collections.emptyList();
        }
    }

    void setSecurityClient(SecurityClient securityClient) {
        this.securityClient = securityClient;
    }
}
