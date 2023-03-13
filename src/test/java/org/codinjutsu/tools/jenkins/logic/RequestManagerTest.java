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

import com.intellij.openapi.project.Project;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.helper.BuildConsoleStreamListener;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.ConsoleLog;
import com.offbytwo.jenkins.model.JobWithDetails;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.model.BuildType;
import org.codinjutsu.tools.jenkins.model.Computer;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.codinjutsu.tools.jenkins.util.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.picocontainer.PicoContainer;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestManagerTest {

    private static final String RUNNING_CONSOLE_OUTPUT = "running";
    private static final String COMPLETED_CONSOLE_OUTPUT = "completed";
    private static final String SUCCESSFUL_CONSOLE_OUTPUT = "successful";
    private static final String FAILED_CONSOLE_OUTPUT = "failed";

    private RequestManager requestManager;

    private JenkinsAppSettings configuration;

    private JenkinsSettings jenkinsSettings;

    @Mock
    private SecurityClient securityClientMock;

    @Mock
    private UrlBuilder urlBuilderMock;

    @Mock
    private Project project;
    @Mock
    private JenkinsServer jenkinsServer;
    @Mock
    private com.offbytwo.jenkins.model.Build runningBuild;
    @Mock
    private com.offbytwo.jenkins.model.Build lastCompletedBuild;
    @Mock
    private com.offbytwo.jenkins.model.Build lastSuccessfulBuild;
    @Mock
    private com.offbytwo.jenkins.model.Build lastFailedBuild;

    private AutoCloseable mocks;

    @Test
    public void loadJenkinsWorkspaceWithMismatchServerPortInTheResponse() throws Exception {
        setServerUrl("http://myjenkins:8080");
        URL urlFromConf = new URL("http://myjenkins:8080");
        URL urlFromJenkins = new URL("http://myjenkins:7070");
        when(urlBuilderMock.createJenkinsWorkspaceUrl(configuration))
                .thenReturn(urlFromConf);
        when(urlBuilderMock.createViewUrl(any(JenkinsPlateform.class), anyString()))
                .thenReturn(urlFromJenkins);
        when(securityClientMock.execute(urlFromConf))
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJenkinsWorkspaceWithIncorrectPortInTheResponse.json")));
        try {
            requestManager.loadJenkinsWorkspace(configuration, jenkinsSettings);
            Assert.fail();
        } catch (ConfigurationException ex) {
            Assert.assertEquals("Jenkins Server Port Mismatch: expected='8080' - actual='7070'. Look at the value of 'Jenkins URL' at http://myjenkins:8080/configure", ex.getMessage());
        }
    }

    @Test
    public void loadJenkinsWorkspaceWithMismatchServerHostInTheResponse() throws Exception {
        setServerUrl("http://myjenkins:8080");
        URL urlFromConf = new URL("http://myjenkins:8080");
        URL urlFromJenkins = new URL("http://anotherjenkins:8080");
        when(urlBuilderMock.createJenkinsWorkspaceUrl(configuration))
                .thenReturn(urlFromConf);
        when(urlBuilderMock.createViewUrl(any(JenkinsPlateform.class), anyString()))
                .thenReturn(urlFromJenkins);
        when(securityClientMock.execute(urlFromConf))
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJenkinsWorkspaceWithIncorrectHostInTheResponse.json")));
        try {
            requestManager.loadJenkinsWorkspace(configuration, jenkinsSettings);
            Assert.fail();
        } catch (ConfigurationException ex) {
            Assert.assertEquals("Jenkins Server Host Mismatch: expected='myjenkins' - actual='anotherjenkins'. Look at the value of 'Jenkins URL' at http://myjenkins:8080/configure", ex.getMessage());
        }
    }

    @Test
    public void loadJenkinsWorkspaceWithValidJenkinsUrlConfiguration() throws Exception {
        final String jenkinsUrlForServer = "http://myjenkins:7070";
        final String serverUrl = "http://myjenkins:8080";
        setServerUrl(serverUrl);
        jenkinsSettings.setJenkinsUrl(jenkinsUrlForServer);
        URL urlFromConf = new URL(serverUrl);
        URL urlFromJenkins = new URL(jenkinsUrlForServer);
        when(urlBuilderMock.createJenkinsWorkspaceUrl(configuration))
                .thenReturn(urlFromConf);
        when(urlBuilderMock.createViewUrl(any(JenkinsPlateform.class), anyString()))
                .thenReturn(urlFromJenkins);
        when(securityClientMock.execute(urlFromConf))
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJenkinsWorkspaceWithIncorrectPortInTheResponse.json")));
        final Jenkins jenkins = requestManager.loadJenkinsWorkspace(configuration, jenkinsSettings);
        Assertions.assertThat(jenkins.getServerUrl()).isEqualTo(serverUrl);
    }

    @Test
    public void loadComputers() throws Exception {
        final String serverUrl = "http://myjenkins:8080";
        final URL computerUrl = new URL("http://myjenkins:8080/computer");
        setServerUrl(serverUrl);
        when(urlBuilderMock.createComputerUrl(serverUrl)).thenReturn(computerUrl);
        when(securityClientMock.execute(computerUrl))
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_computer.json")));
        final List<Computer> computers = requestManager.loadComputer(configuration);
        assertThat(computers).hasSize(2);
    }

    @Test
    public void loadConsoleTextForRunningBuild() {
        final Job job = createJobWithBuilds(runningBuild);
        final String buildOutput = loadConsoleTextFor(job, BuildType.LAST);
        assertThat(buildOutput).isEqualTo(RUNNING_CONSOLE_OUTPUT);
    }

    @Test
    public void loadConsoleTextForLastBuild() {
        final Job job = createJobWithBuilds();
        final String buildOutput = loadConsoleTextFor(job, BuildType.LAST);
        assertThat(buildOutput).isEqualTo(COMPLETED_CONSOLE_OUTPUT);
    }

    @Test
    public void loadConsoleTextForLastSuccessfulBuild() {
        final Job job = createJobWithBuilds();
        final String buildOutput = loadConsoleTextFor(job, BuildType.LAST_SUCCESSFUL);
        assertThat(buildOutput).isEqualTo(SUCCESSFUL_CONSOLE_OUTPUT);
    }

    @Test
    public void loadConsoleTextForLastFailedBuild() {
        final Job job = createJobWithBuilds();
        final var buildOutput = loadConsoleTextFor(job, BuildType.LAST_FAILED);
        assertThat(buildOutput).isEqualTo(FAILED_CONSOLE_OUTPUT);
    }

    @SneakyThrows
    private String loadConsoleTextFor(Job job, BuildType buildType) {
        final var completableFuture = new CompletableFuture<String>();
        final var result = new StringBuilder();
        requestManager.loadConsoleTextFor(job, buildType, new BuildConsoleStreamListener() {
            @Override
            public void onData(String newLogChunk) {
                // Ignore auto generated message
                if (!newLogChunk.startsWith("Log for Build ")) {
                    result.append(newLogChunk);
                }
            }

            @Override
            public void finished() {
                completableFuture.complete(result.toString());
            }
        });
        return completableFuture.get();
    }

    @NotNull
    private Job createJobWithBuilds() {
        return createJobWithBuilds(lastCompletedBuild);
    }

    @NotNull
    private Job createJobWithBuilds(com.offbytwo.jenkins.model.Build lastBuild) {
        final Job job = mock(Job.class, Answers.RETURNS_SMART_NULLS);
        final String fullJobName = "fullJobName";
        when(job.getFullName()).thenReturn(fullJobName);
        JobWithDetails jobWithDetails = mock(JobWithDetails.class, Answers.RETURNS_SMART_NULLS);
        try {
            when(jenkinsServer.getJob(fullJobName)).thenReturn(jobWithDetails);
            when(jobWithDetails.getLastBuild()).thenReturn(lastBuild);
            when(jobWithDetails.getLastCompletedBuild()).thenReturn(lastCompletedBuild);
            when(jobWithDetails.getLastSuccessfulBuild()).thenReturn(lastSuccessfulBuild);
            when(jobWithDetails.getLastFailedBuild()).thenReturn(lastFailedBuild);
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
        return job;
    }

    @After
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Before
    public void setUp() throws IOException {
        mocks = MockitoAnnotations.openMocks(this);
        configuration = new JenkinsAppSettings();
        jenkinsSettings = new JenkinsSettings();
        when(project.getService(UrlBuilder.class)).thenReturn(urlBuilderMock);
        final PicoContainer container = mock(PicoContainer.class);
        when(project.getPicoContainer()).thenReturn(container);
        when(container.getComponentInstance(UrlBuilder.class.getName())).thenReturn(urlBuilderMock);
        requestManager = new RequestManager(project);
        requestManager.setSecurityClient(securityClientMock);
        requestManager.setJenkinsServer(jenkinsServer);
        Whitebox.setInternalState(requestManager, urlBuilderMock);

        when(urlBuilderMock.toUrl(anyString())).thenCallRealMethod();
        when(urlBuilderMock.createConfigureUrl(anyString())).thenCallRealMethod();
        when(urlBuilderMock.removeTrailingSlash(anyString())).thenCallRealMethod();

        mockBuildConsoleOutput(runningBuild, RUNNING_CONSOLE_OUTPUT);
        mockBuildConsoleOutput(lastCompletedBuild, COMPLETED_CONSOLE_OUTPUT);
        mockBuildConsoleOutput(lastSuccessfulBuild, SUCCESSFUL_CONSOLE_OUTPUT);
        mockBuildConsoleOutput(lastFailedBuild, FAILED_CONSOLE_OUTPUT);

        when(runningBuild.details().isBuilding()).thenReturn(true);
    }

    private void setServerUrl(String serverUrl) {
        configuration.setServerUrl(serverUrl);
        jenkinsSettings.setJenkinsUrl("");
    }

    private void mockBuildConsoleOutput(com.offbytwo.jenkins.model.Build build, String consoleText) throws IOException {
        final var url = "https://dummyserver.dev/jenkins";
        final BuildWithDetails buildWithDetails = mock(BuildWithDetails.class, Answers.RETURNS_SMART_NULLS);
        when(build.getUrl()).thenReturn(url);
        when(build.details()).thenReturn(buildWithDetails);
        when(buildWithDetails.getUrl()).thenReturn(url);

        final var hasMoreData = false;
        final var currentBufferSize = 0;
        final var consoleLog = new ConsoleLog(consoleText, hasMoreData, currentBufferSize);
        when(buildWithDetails.getConsoleOutputText(anyInt())).thenReturn(consoleLog);
    }
}
