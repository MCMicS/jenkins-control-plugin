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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.net.IdeHttpClientHelpers;
import com.intellij.util.net.ssl.CertificateManager;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.helper.BuildConsoleStreamListener;
import com.offbytwo.jenkins.model.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.exception.JenkinsPluginRuntimeException;
import org.codinjutsu.tools.jenkins.exception.NoJobFoundException;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Computer;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RequestManager implements RequestManagerInterface, Disposable {

    private static final Logger logger = Logger.getInstance(RequestManager.class);

    private static final String BUILDHIVE_CLOUDBEES = "buildhive";

    private final Project project;

    private final UrlBuilder urlBuilder;
    private final RssParser rssParser = new RssParser();
    private final JenkinsParser jsonParser = new JenkinsJsonParser();
    private SecurityClient securityClient;
    private JenkinsPlateform jenkinsPlateform = JenkinsPlateform.CLASSIC;
    private JenkinsServer jenkinsServer;

    public RequestManager(Project project) {
        this.project = project;
        this.urlBuilder = UrlBuilder.getInstance(project);
    }

    public static RequestManager getInstance(Project project) {
        return project.getService(RequestManager.class);
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

        final Jenkins jenkins = jsonParser.createWorkspace(jenkinsWorkspaceData);
        final ConfigurationValidator.ValidationResult validationResult = ConfigurationValidator.getInstance(project)
                .validate(configuration.getServerUrl(), jenkins.getServerUrl());
        if (!validationResult.isValid()) {
            throw new ConfigurationException(validationResult.getFirstError());
        }
        return jenkins;
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
                if (NodeParameterRenderer.NODE_PARAMETER.equals(jobParameter.getJobParameterType())) {
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
    public void runBuild(Job job, JenkinsAppSettings configuration, Map<String, ?> parameters) {
        if (handleNotYetLoggedInState()) return;
        if (job.hasParameters() && parameters.size() > 0) {
            parameters.keySet().removeIf(key -> !job.hasParameter(key));
        }
        final AtomicInteger fileCount = new AtomicInteger();

        final Collection<RequestData> requestData = new LinkedHashSet<>(parameters.size());
        parameters.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof VirtualFile)
                .map(entry -> new FileParameter(entry.getKey(), (VirtualFile) entry.getValue(),
                        () -> String.format("file%d", fileCount.getAndIncrement())))
                .forEach(requestData::add);
        parameters.entrySet().stream().filter(entry -> entry.getValue() instanceof String)
                .map(entry -> new StringParameter(entry.getKey(), (String) entry.getValue()))
                .forEach(requestData::add);
        runBuild(job, configuration, requestData);
    }

    private void runBuild(Job job, JenkinsAppSettings configuration, Collection<RequestData> requestData) {
        if (handleNotYetLoggedInState()) return;
        URL url = urlBuilder.createRunJobUrl(job.getUrl(), configuration);
        securityClient.execute(url, requestData);
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
        final String serverUrl = jenkinsAppSettings.getServerUrl();
        securityClient.connect(urlBuilder.createAuthenticationUrl(serverUrl));

        final JenkinsHttpClient jenkinsHttpClient = new JenkinsHttpClient(urlBuilder.createServerUrl(serverUrl),
                createHttpClientBuilder(serverUrl, jenkinsSettings), jenkinsSettings.getUsername(),
                jenkinsSettings.getPassword());
        jenkinsServer = new JenkinsServer(jenkinsHttpClient);
    }

    @Override
    public String testAuthenticate(String serverUrl, String username, String password, String crumbData, JenkinsVersion version,
                                   int connectionTimoutInSeconds) {
        SecurityClientFactory.setVersion(version);
        final int connectionTimout = getConnectionTimout(connectionTimoutInSeconds);
        final SecurityClient securityClientForTest;
        if (StringUtils.isNotBlank(username)) {
            securityClientForTest = SecurityClientFactory.basic(username, password, crumbData, connectionTimout);
        } else {
            securityClientForTest = SecurityClientFactory.none(crumbData, connectionTimout);
        }
        final String serverData = securityClientForTest.connect(urlBuilder.createAuthenticationUrl(serverUrl));
        return jsonParser.getServerUrl(serverData);
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

    @NotNull
    @Override
    public List<Job> loadJenkinsView(@NotNull View view) {
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
    public String loadConsoleTextFor(Job job, BuildType buildType) {
        try {
            final com.offbytwo.jenkins.model.Build build = getBuildForType(buildType).apply(getJob(job));
            return build.equals(com.offbytwo.jenkins.model.Build.BUILD_HAS_NEVER_RUN) ? null :
                    build.details().getConsoleOutputText();
        } catch (IOException e) {
            logger.warn("cannot load log for " + job.getNameToRenderSingleJob());
            return null;
        }
    }

    @Override
    public void loadConsoleTextFor(Job job, BuildType buildType,
                                     BuildConsoleStreamListener buildConsoleStreamListener) {
        try {
            final int pollingInSeconds = 1;
            final int poolingTimeout = Math.toIntExact(TimeUnit.HOURS.toSeconds(1));
            final com.offbytwo.jenkins.model.Build build = getBuildForType(buildType).apply(getJob(job));
            if (build.equals(com.offbytwo.jenkins.model.Build.BUILD_HAS_NEVER_RUN)) {
                buildConsoleStreamListener.onData("No Build available\n");
                buildConsoleStreamListener.finished();
            } else {
                buildConsoleStreamListener.onData("Log for Build " + build.getUrl() + "console\n");
                streamConsoleOutput(build.details(), buildConsoleStreamListener, pollingInSeconds, poolingTimeout);
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("cannot load log for " + job.getNameToRenderSingleJob());
            Thread.currentThread().interrupt();
            buildConsoleStreamListener.finished();
        }
    }

    private void streamConsoleOutput(BuildWithDetails buildWithDetails,
                                     BuildConsoleStreamListener listener,
                                     int poolingInterval,
                                     int poolingTimeout) throws InterruptedException, IOException {
        // Calculate start and timeout
        final long startTime = System.currentTimeMillis();
        final long timeoutTime = startTime + (poolingTimeout * 1000L);
        final long sleepMillis = poolingInterval * 1000L;

        final AtomicInteger bufferOffset = new AtomicInteger(0);
        while (true) {
            //noinspection BusyWait
            Thread.sleep(sleepMillis);
            ProgressManager.checkCanceled();
            final ConsoleLog consoleLog = buildWithDetails.getConsoleOutputText(bufferOffset.get());
            String logString = consoleLog.getConsoleLog();
            if (logString != null && !logString.isEmpty()) {
                listener.onData(logString);
            }
            if (Boolean.TRUE.equals(consoleLog.getHasMoreData())) {
                bufferOffset.set(consoleLog.getCurrentBufferSize());
            } else {
                listener.finished();
                break;
            }
            if (System.currentTimeMillis() > timeoutTime) {
                throw new JenkinsPluginRuntimeException(String.format("Pooling for build %s - %d timeout! " +
                                "Check if job stuck in jenkins",
                        buildWithDetails.getDisplayName(), buildWithDetails.getNumber()));
            }
        }
    }

    @NotNull
    private Function<JobWithDetails, com.offbytwo.jenkins.model.Build> getBuildForType(BuildType buildType) {
        final Function<JobWithDetails, com.offbytwo.jenkins.model.Build> buildProvider;
        switch (buildType) {
            case LAST_SUCCESSFUL:
                buildProvider = JobWithDetails::getLastSuccessfulBuild;
                break;
            case LAST_FAILED:
                buildProvider = JobWithDetails::getLastFailedBuild;
                break;
            case LAST://Fallthrough
            default:
                buildProvider = preferLastBuildRunning(JobWithDetails::getLastCompletedBuild);
        }
        return buildProvider;
    }

    @NotNull
    private Function<JobWithDetails, com.offbytwo.jenkins.model.Build> preferLastBuildRunning(
            Function<JobWithDetails, com.offbytwo.jenkins.model.Build> fallback) {
        return job -> {
            try {
                com.offbytwo.jenkins.model.Build lastBuild = job.getLastBuild();
                if (lastBuild.details().isBuilding()) {
                    return lastBuild;
                }
            } catch (IOException e) {
                logger.warn("cannot load details for " + job.getName());
            }
            return fallback.apply(job);
        };
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
            logger.warn("cannot load test results for " + job.getNameToRenderSingleJob());
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
    private HttpClientBuilder createHttpClientBuilder(String serverUrl, JenkinsSettings jenkinsSettings) {
        final CredentialsProvider provider = new BasicCredentialsProvider();
        IdeHttpClientHelpers.ApacheHttpClient4.setProxyCredentialsForUrlIfEnabled(provider, serverUrl);
        final RequestConfig.Builder requestConfig = RequestConfig.custom();
        IdeHttpClientHelpers.ApacheHttpClient4.setProxyForUrlIfEnabled(requestConfig, serverUrl);
        final HttpClientBuilder builder = HttpClients.custom()
                .setSSLContext(CertificateManager.getInstance().getSslContext())
                .setDefaultRequestConfig(requestConfig.build())
                .setDefaultCredentialsProvider(provider);

        if (StringUtils.isNotBlank(jenkinsSettings.getCrumbData())) {
            builder.setDefaultHeaders(Collections.singletonList(
                    new BasicHeader(jenkinsSettings.getVersion().getCrumbName(), jenkinsSettings.getCrumbData())));
        }
        return builder;
    }

    @NotNull
    private JobWithDetails getJob(@NotNull Job job) {
        final Optional<JobWithDetails> jobWithDetails;
        try {
            // maybe refactor and use job url
            jobWithDetails = Optional.ofNullable(jenkinsServer.getJob(job.getFullName()));
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
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

    void setJenkinsServer(JenkinsServer jenkinsServer) {
        this.jenkinsServer = jenkinsServer;
    }

    @Override
    public void dispose() {
        Optional.ofNullable(jenkinsServer).ifPresent(JenkinsServer::close);
    }
}
