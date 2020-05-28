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
import org.assertj.core.api.Assertions;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.model.Computer;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.codinjutsu.tools.jenkins.util.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.picocontainer.PicoContainer;
import org.powermock.reflect.Whitebox;

import java.net.URL;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestManagerTest {

    private RequestManager requestManager;

    private JenkinsAppSettings configuration;

    @Mock
    private SecurityClient securityClientMock;

    @Mock
    private UrlBuilder urlBuilderMock;

    @Mock
    private Project project;

    @Test
    public void loadJenkinsWorkspaceWithMismatchServerPortInTheResponse() throws Exception {
        configuration.setServerUrl("http://myjenkins:8080");
        URL urlFromConf = new URL("http://myjenkins:8080");
        URL urlFromJenkins = new URL("http://myjenkins:8082");
        when(urlBuilderMock.createJenkinsWorkspaceUrl(configuration))
                .thenReturn(urlFromConf);
        when(urlBuilderMock.createViewUrl(any(JenkinsPlateform.class), anyString()))
                .thenReturn(urlFromJenkins);
        when(securityClientMock.execute(urlFromConf))
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJenkinsWorkspaceWithIncorrectPortInTheResponse.json")));
        try {
            requestManager.loadJenkinsWorkspace(configuration);
            Assert.fail();
        } catch (ConfigurationException ex) {
            Assert.assertEquals("Jenkins Server Port Mismatch: expected='8080' - actual='8082'. Look at the value of 'Jenkins URL' at http://myjenkins:8080/configure", ex.getMessage());
        }
    }

    @Test
    public void loadJenkinsWorkspaceWithMismatchServerHostInTheResponse() throws Exception {
        configuration.setServerUrl("http://myjenkins:8080");
        URL urlFromConf = new URL("http://myjenkins:8080");
        URL urlFromJenkins = new URL("http://anotherjenkins:8080");
        when(urlBuilderMock.createJenkinsWorkspaceUrl(configuration))
                .thenReturn(urlFromConf);
        when(urlBuilderMock.createViewUrl(any(JenkinsPlateform.class), anyString()))
                .thenReturn(urlFromJenkins);
        when(securityClientMock.execute(urlFromConf))
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJenkinsWorkspaceWithIncorrectPortInTheResponse.json")));
        try {
            requestManager.loadJenkinsWorkspace(configuration);
            Assert.fail();
        } catch (ConfigurationException ex) {
            Assert.assertEquals("Jenkins Server Host Mismatch: expected='myjenkins' - actual='anotherjenkins'. Look at the value of 'Jenkins URL' at http://myjenkins:8080/configure", ex.getMessage());
        }
    }

    @Test
    public void loadComputers() throws Exception {
        final String serverUrl = "http://myjenkins:8080";
        final URL computerUrl = new URL("http://myjenkins:8080/computer");
        configuration.setServerUrl(serverUrl);
        when(urlBuilderMock.createComputerUrl(serverUrl)).thenReturn(computerUrl);
        when(securityClientMock.execute(computerUrl))
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_computer.json")));
        final List<Computer> computers = requestManager.loadComputer(configuration);
        Assertions.assertThat(computers).hasSize(2);
    }


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configuration = new JenkinsAppSettings();
//        when(project.getService(UrlBuilder.class, true)).thenReturn(urlBuilderMock);
        final PicoContainer container = mock(PicoContainer.class);
        when(project.getPicoContainer()).thenReturn(container);
        when(container.getComponentInstance(UrlBuilder.class.getName())).thenReturn(urlBuilderMock);
        requestManager = new RequestManager(project);
        requestManager.setSecurityClient(securityClientMock);
        Whitebox.setInternalState(requestManager, urlBuilderMock);
    }
}
