/*
 * Copyright (c) 2011 David Boissier
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

import org.apache.commons.httpclient.util.URIUtil;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class UrlBuilderTest {
    private JenkinsConfiguration configuration;

    private UrlBuilder urlBuilder;


    @Test
    public void createRunJobUrl() throws Exception {
        configuration.setDelay(20);

        URL url = urlBuilder.createRunJobUrl("http://localhost:8080/jenkins/My Job", configuration);
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/My%20Job/build?delay=20sec"));
    }


    @Test
    public void createJenkinsWorkspaceUrl() throws Exception {
        configuration.setServerUrl("http://localhost:8080/jenkins");

        URL url = urlBuilder.createJenkinsWorkspaceUrl(configuration);
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/api/xml?tree=nodeName,nodeDescription,primaryView%5Bname,url%5D,views%5Bname,url%5D"));
    }


    @Test
    public void createViewUrl() throws Exception {
        URL url = urlBuilder.createViewUrl("http://localhost:8080/jenkins/My View");
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/My%20View/api/xml?tree=name,url,jobs%5Bname,url,color,inQueue,healthReport%5BiconUrl%5D,lastBuild%5Burl,building,result,number%5D%5D"));
    }


    @Test
    public void createJobUrl() throws Exception {
        URL url = urlBuilder.createJobUrl("http://localhost:8080/jenkins/my Job");
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/my%20Job/api/xml?tree=name,url,color,inQueue,healthReport%5BiconUrl%5D,lastBuild%5Burl,building,result,number%5D"));
    }


    @Test
    public void createRssLastBuildUrl() throws Exception {
        URL url = urlBuilder.createRssLatestUrl("http://localhost:8080/jenkins");
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/rssLatest"));
    }

    @Before
    public void setUp() {
        configuration = new JenkinsConfiguration();
        urlBuilder = new UrlBuilder();
    }


}
