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

import org.apache.commons.io.IOUtils;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URL;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class RequestManagerTest {

    private RequestManager requestManager;

    private JenkinsAppSettings configuration;

    @Mock
    private SecurityClient securityClientMock;


    @Test
    public void loadJenkinsWorkspaceWithIncorrectServerPortInTheResponse() throws Exception {
        configuration.setServerUrl("http://myjenkins:8080");
        when(securityClientMock.execute(any(URL.class)))
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("JsonRequestManager_loadJenkinsWorkspaceWithIncorrectPortInTheResponse.json")));
        try {
            requestManager.loadJenkinsWorkspace(configuration);
            Assert.fail();
        } catch (ConfigurationException ex) {
            Assert.assertEquals("Jenkins Port seems to be incorrect in the Server configuration page. Please fix 'Jenkins URL' at http://myjenkins:8080/configure", ex.getMessage());
        }
    }


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configuration = new JenkinsAppSettings();
        requestManager = new RequestManager(securityClientMock);
    }
}
