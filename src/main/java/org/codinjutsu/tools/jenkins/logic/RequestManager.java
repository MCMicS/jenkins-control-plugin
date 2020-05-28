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
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.TestChildReport;
import com.offbytwo.jenkins.model.TestResult;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.exception.NoJobFoundException;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.security.JenkinsVersion;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.codinjutsu.tools.jenkins.security.SecurityClientFactory;
import org.codinjutsu.tools.jenkins.view.parameter.NodeParameterRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RequestManager implements RequestManagerInterface {

    private static final Logger logger = Logger.getLogger(RequestManager.class);

    private static final String BUILDHIVE_CLOUDBEES = "buildhive";

    private final Project project;

    private final UrlBuilder urlBuilder;

    private SecurityClient securityClient;

    private JenkinsPlateform jenkinsPlateform = JenkinsPlateform.CLASSIC;

    private RssParser rssParser = new RssParser();

    private JenkinsParser jsonParser = new JenkinsJsonParser();
    private JenkinsServer jenkinsServer;

    public RequestManager(Project project) {
        this.project = project;
        this.urlBuilder = UrlBuilder.getInstance(project);
    }

    public static RequestManager getInstance(Project project) {
        return ServiceManager.getService(project, RequestManager.class);
    }

    private static boolean canContainNestedJobs(@NotNull Job job) {
        return job.getJobType().containNestedJobs();
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
        final List<Job> jobsFromView;
        if (jenkinsPlateform.equals(JenkinsPlateform.CLASSIC)) {
            jobsFromView = jsonParser.createJobs(jenkinsViewData);
        } else {
            jobsFromView = jsonParser.createCloudbeesViewJobs(jenkinsViewData);
        }
        return withNestedJobs(jobsFromView);
    }

    @NotNull
    private List<Job> withNestedJobs(@NotNull List<Job> jobs) {
        final List<Job> jobWithNested = new ArrayList<>();
        for (Job job : jobs) {
            if (canContainNestedJobs(job)) {
                jobWithNested.add(withNestedJobs(job));
            } else {
                jobWithNested.add(job);
            }
        }
        return withNodeParameterFix(jobWithNested);
    }

    /**
     * @deprecated 2020-05-26 remove if NodeParameter implement choices API
     */
    @Deprecated
    @NotNull
    private List<Job> withNodeParameterFix(@NotNull List<Job> jobs) {
        final AtomicReference<List<Computer>> computers = new AtomicReference<>();
        final Supplier<Collection<Computer>> getOrLoad = () -> {
            List<Computer> cachedComputers = computers.get();
            if (cachedComputers == null) {
                cachedComputers = loadComputer(JenkinsAppSettings.getSafeInstance(project));
                computers.set(cachedComputers);
            }
            return cachedComputers;
        };
        // Suppliers.memoize(() currently mot working
        return jobs.stream().map(job -> withNodeParameterFix(job, getOrLoad)).collect(Collectors.toList());
    }

    /**
     * @deprecated 2020-05-26 remove if NodeParameter implement choices API
     */
    @Deprecated
    @NotNull
    private Job withNodeParameterFix(@NotNull Job job, @NotNull Supplier<Collection<Computer>> computers) {
        final boolean fixJob = job.getParameters().stream().map(JobParameter::getJobParameterType)
                .anyMatch(NodeParameterRenderer.NODE_PARAMETER::equals);
        if (fixJob) {
            final Job.JobBuilder fixedJob = job.toBuilder();
            fixedJob.clearParameters();
            for (JobParameter jobParameter : job.getParameters()) {
                if (jobParameter.getJobParameterType().equals(NodeParameterRenderer.NODE_PARAMETER)) {
                    final JobParameter.JobParameterBuilder fixedJobParameter = jobParameter.toBuilder();
                    final List<String> computerNames = computers.get().stream().map(Computer::getDisplayName)
                            .collect(Collectors.toList());
                    fixedJobParameter.choices(computerNames);
                    fixedJob.parameter(fixedJobParameter.build());
                } else {
                    fixedJob.parameter(jobParameter);
                }
            }
            return fixedJob.build();
        }

        return job;
    }

    @NotNull
    private Job withNestedJobs(@NotNull Job job) {
        job.setNestedJobs(withNestedJobs(loadNestedJobs(job.getUrl())));
        return job;
    }

    private List<Job> loadNestedJobs(String currentJobUrl) {
        if (handleNotYetLoggedInState()) return Collections.emptyList();
        URL url = urlBuilder.createNestedJobUrl(currentJobUrl);
        return jsonParser.createJobs(securityClient.execute(url));
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

    @NotNull
    private Job loadJob(String jenkinsJobUrl) {
        if (handleNotYetLoggedInState()) return createEmptyJob(jenkinsJobUrl);
        URL url = urlBuilder.createJobUrl(jenkinsJobUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createJob(jenkinsJobData);
    }

    @NotNull
    private Job createEmptyJob(String jenkinsJobUrl) {
        return Job.builder().name("").buildable(false).fullName("").url(jenkinsJobUrl)
                .parameters(Collections.emptyList()).inQueue(false).build();
    }

    private void stopBuild(String jenkinsBuildUrl) {
        if (handleNotYetLoggedInState()) return;
        URL url = urlBuilder.createStopBuildUrl(jenkinsBuildUrl);
        securityClient.execute(url);
    }

    @NotNull
    private Build loadBuild(String jenkinsBuildUrl) {
        if (handleNotYetLoggedInState()) return Build.NULL;
        URL url = urlBuilder.createBuildUrl(jenkinsBuildUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createBuild(jenkinsJobData);
    }

    private List<Build> loadBuilds(String jenkinsBuildUrl) {
        if (handleNotYetLoggedInState()) return Collections.emptyList();
        URL url = urlBuilder.createBuildsUrl(jenkinsBuildUrl);
        String jenkinsJobData = securityClient.execute(url);
        return jsonParser.createBuilds(jenkinsJobData);
    }

    @Override
    public void runBuild(Job job, JenkinsAppSettings configuration, Map<String, VirtualFile> files) {
        if (handleNotYetLoggedInState()) return;
        if (job.hasParameters() && files.size() > 0) {
            files.keySet().removeIf(key -> !job.hasParameter(key));
            securityClient.setFiles(files);
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
        final int connectionTimout = getConnectionTimout(jenkinsSettings.getConnectionTimeout());
        if (jenkinsSettings.isSecurityMode()) {
            securityClient = SecurityClientFactory.basic(jenkinsSettings.getUsername(), jenkinsSettings.getPassword(),
                    jenkinsSettings.getCrumbData(), connectionTimout);
        } else {
            securityClient = SecurityClientFactory.none(jenkinsSettings.getCrumbData(), connectionTimout);
        }
        securityClient.connect(urlBuilder.createAuthenticationUrl(jenkinsAppSettings.getServerUrl()));

        jenkinsServer = new JenkinsServer(urlBuilder.createServerUrl(jenkinsAppSettings.getServerUrl()), jenkinsSettings.getUsername(), jenkinsSettings.getPassword());
    }

    @Override
    public void testAuthenticate(String serverUrl, String username, String password, String crumbData, JenkinsVersion version,
                                 int connectionTimoutInSeconds) {
        SecurityClientFactory.setVersion(version);
        final int connectionTimout = getConnectionTimout(connectionTimoutInSeconds);
        final SecurityClient securityClientForTest;
        if (StringUtils.isNotBlank(username)) {
            securityClientForTest = SecurityClientFactory.basic(username, password, crumbData, connectionTimout);
        } else {
            securityClientForTest = SecurityClientFactory.none(crumbData, connectionTimout);
        }
        securityClientForTest.connect(urlBuilder.createAuthenticationUrl(serverUrl));
    }

    @Override
    public List<Job> loadFavoriteJobs(List<JenkinsSettings.FavoriteJob> favoriteJobs) {
        if (handleNotYetLoggedInState()) return Collections.emptyList();
        final List<Job> jobs = new LinkedList<>();
        for (JenkinsSettings.FavoriteJob favoriteJob : favoriteJobs) {
            jobs.add(loadJob(favoriteJob.getUrl()));
        }
        return withNestedJobs(jobs);
    }

    @Override
    public void stopBuild(Build build) {
        stopBuild(build.getUrl());
    }

    @Override
    public @NotNull Job loadJob(Job job) {
        return withNodeParameterFix(loadJob(job.getUrl()), () -> loadComputer(JenkinsAppSettings.getSafeInstance(project)));
    }

    @Override
    public List<Job> loadJenkinsView(View view) {
        return loadJenkinsView(view.getUrl());
    }

    @Override
    public List<Build> loadBuilds(Job job) {
        return loadBuilds(job.getUrl());
    }

    @NotNull
    @Override
    public Build loadBuild(Build build) {
        return loadBuild(build.getUrl());
    }

    @Override
    public String loadConsoleTextFor(Job job) {
        try {
            return getJob(job).getLastCompletedBuild().details().getConsoleOutputText();
        } catch (IOException e) {
            logger.warn("cannot load log for " + job.getName());
            return null;
        }
    }

    @Override
    public List<TestResult> loadTestResultsFor(Job job) {
        try {
            List<TestResult> result = new ArrayList<>();
            com.offbytwo.jenkins.model.Build lastCompletedBuild = getJob(job).getLastCompletedBuild();
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

    @NotNull
    @Override
    public List<Computer> loadComputer(JenkinsAppSettings settings) {
        if (handleNotYetLoggedInState()) {
            return Collections.emptyList();
        }
        final URL url = urlBuilder.createComputerUrl(settings.getServerUrl());
        return jsonParser.createComputers(securityClient.execute(url));
    }

    @NotNull
    private JobWithDetails getJob(@NotNull Job job) {
        Optional<JobWithDetails> jobWithDetails = Optional.empty();
        try {
            // maybe refactor and use job url
            jobWithDetails = Optional.ofNullable(jenkinsServer.getJob(job.getFullName()));
        } catch (IOException e) {
            throw new NoJobFoundException(job, e);
        }
        return jobWithDetails.orElseThrow(() -> new NoJobFoundException(job));
    }

    private int getConnectionTimout(int connectionTimoutInSeconds) {
        return connectionTimoutInSeconds * 1000;
    }

    void setSecurityClient(SecurityClient securityClient) {
        this.securityClient = securityClient;
    }
}
