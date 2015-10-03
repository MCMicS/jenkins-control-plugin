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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.codinjutsu.tools.jenkins.security.SecurityClientFactory;

import javax.swing.*;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//FIXME not to be used in ui thread
public class RequestManager {

    private static final Logger logger = Logger.getLogger(RequestManager.class);

    private static final String BUILDHIVE_CLOUDBEES = "buildhive";

    private UrlBuilder urlBuilder;

    private SecurityClient securityClient;

    private JenkinsPlateform jenkinsPlateform = JenkinsPlateform.CLASSIC;

    private RssParser rssParser = new RssParser();

    private JenkinsParser jsonParser = new JenkinsJsonParser();

    public static RequestManager getInstance(Project project) {
        return ServiceManager.getService(project, RequestManager.class);
    }


    public RequestManager(Project project) {
        this.urlBuilder = UrlBuilder.getInstance(project);
    }

    RequestManager(UrlBuilder urlBuilder, SecurityClient securityClient) {
        this.urlBuilder = urlBuilder;
        this.securityClient = securityClient;
    }

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
     * @param configuration
     * @return
     */
    public Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsAppSettings configuration) {
        if (handleNotYetLoggedInState()) return Collections.emptyMap();
        URL url = urlBuilder.createRssLatestUrl(configuration.getServerUrl());

        String rssData = securityClient.execute(url);

        return rssParser.loadJenkinsRssLatestBuilds(rssData);
    }

    public List<Job> loadJenkinsView(String viewUrl) {
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
        if(SwingUtilities.isEventDispatchThread()){
            logger.warn("RequestManager.handleNotYetLoggedInState called from EDT");
            threadStack = true;
        }else{
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                logger.log(Level.WARN, "Interuppted while ...", e);
            }
        }
        if(securityClient == null){
            logger.warn("Not yet logged in, all calls until login will fail");
            threadStack = true;
            result = true;
        }
        if(threadStack)
            Thread.dumpStack();
        return result;
    }

    public Job loadJob(String jenkinsJobUrl) {
        if (handleNotYetLoggedInState()) return null;
        URL url = urlBuilder.createJobUrl(jenkinsJobUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createJob(jenkinsJobData);
    }

    public Build loadBuild(String jenkinsBuildUrl) {
        if (handleNotYetLoggedInState()) return null;
        URL url = urlBuilder.createBuildUrl(jenkinsBuildUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createBuild(jenkinsJobData);
    }

    public void runBuild(Job job, JenkinsAppSettings configuration, Map<String, VirtualFile> files) {
        if (handleNotYetLoggedInState()) return;
        if (job.hasParameters()) {
            if (files.size() > 0) {
                for(String key: files.keySet()) {
                    if (!job.hasParameter(key)) {
                        files.remove(files.get(key));
                    }
                }
                securityClient.setFiles(files);
            }
        }
        runBuild(job, configuration);
    }

    public void runBuild(Job job, JenkinsAppSettings configuration) {
        if (handleNotYetLoggedInState()) return ;
        URL url = urlBuilder.createRunJobUrl(job.getUrl(), configuration);
        securityClient.execute(url);
    }

    public void runParameterizedBuild(Job job, JenkinsAppSettings configuration, Map<String, String> paramValueMap) {
        if (handleNotYetLoggedInState()) return ;
        URL url = urlBuilder.createRunParameterizedJobUrl(job.getUrl(), configuration, paramValueMap);
        securityClient.execute(url);
    }

    public void authenticate(JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings) {
        if (jenkinsSettings.isSecurityMode()) {
            securityClient = SecurityClientFactory.basic(jenkinsSettings.getUsername(), jenkinsSettings.getPassword(), jenkinsSettings.getCrumbData());
        } else {
            securityClient = SecurityClientFactory.none(jenkinsSettings.getCrumbData());
        }
        securityClient.connect(urlBuilder.createAuthenticationUrl(jenkinsAppSettings.getServerUrl()));
    }

    public void authenticate(String serverUrl, String username, String password, String crumbData) {
        if (StringUtils.isNotBlank(username)) {
            securityClient = SecurityClientFactory.basic(username, password, crumbData);
        } else {
            securityClient = SecurityClientFactory.none(crumbData);
        }
        securityClient.connect(urlBuilder.createAuthenticationUrl(serverUrl));
    }

    public List<Job> loadFavoriteJobs(List<JenkinsSettings.FavoriteJob> favoriteJobs) {
        if (handleNotYetLoggedInState()) return Collections.emptyList();
        List<Job> jobs = new LinkedList<Job>();
        for (JenkinsSettings.FavoriteJob favoriteJob : favoriteJobs) {
            jobs.add(loadJob(favoriteJob.url));
        }
        return jobs;
    }
}
