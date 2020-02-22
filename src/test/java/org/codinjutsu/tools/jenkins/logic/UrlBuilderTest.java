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

import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class UrlBuilderTest {
    private JenkinsAppSettings configuration;

    private UrlBuilder urlBuilder;


    @Test
    public void createRunJobUrl() throws Exception {
        configuration.setDelay(20);

        URL url = urlBuilder.createRunJobUrl("http://localhost:8080/jenkins/My%20Job", configuration);
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/My%20Job/build?delay=20sec"));
    }

    @Test
    public void createRunParameterizedJobUrl() throws Exception {
        configuration.setDelay(20);

        Map<String, String> valueByNameParams = new LinkedHashMap<String, String>();
        valueByNameParams.put("param1", "value1");
        valueByNameParams.put("param2", "value2");

        URL url = urlBuilder.createRunParameterizedJobUrl("http://localhost:8080/jenkins/My%20Job", configuration, valueByNameParams);
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/My%20Job/buildWithParameters?delay=20sec&param1=value1&param2=value2"));
    }

    @Test
    public void createJenkinsWorkspaceUrl() throws Exception {
        configuration.setServerUrl("http://localhost:8080/jenkins");

        URL url = urlBuilder.createJenkinsWorkspaceUrl(configuration);
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/api/json?tree=nodeName,nodeDescription,primaryView%5Bname,url%5D,views%5Bname,url,views%5Bname,url%5D%5D"));
    }

    @Test
    public void createViewUrlForClassicPlateform() throws Exception {
        URL url = urlBuilder.createViewUrl(JenkinsPlateform.CLASSIC, "http://localhost:8080/jenkins/My%20View");
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/My%20View/api/json?tree=name,url,jobs%5Bname,fullName,displayName,fullDisplayName,jobs,url,color,buildable,inQueue,healthReport%5Bdescription,iconUrl%5D,lastBuild%5Bid,url,building,result,number,timestamp,duration%5D,property%5BparameterDefinitions%5Bname,type,defaultParameterValue%5Bvalue%5D,description,choices%5D%5D%5D"));
    }

    @Test
    public void createJobJSONUrl() throws Exception {
        URL url = urlBuilder.createJobUrl("http://localhost:8080/jenkins/my%20Job");
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/my%20Job/api/json?tree=name,fullName,displayName,fullDisplayName,jobs,url,color,buildable,inQueue,healthReport%5Bdescription,iconUrl%5D,lastBuild%5Bid,url,building,result,number,timestamp,duration%5D,property%5BparameterDefinitions%5Bname,type,defaultParameterValue%5Bvalue%5D,description,choices%5D%5D"));
    }

    @Test
    public void createViewUrlForCloudbeesPlateform() throws Exception {
        URL url = urlBuilder.createViewUrl(JenkinsPlateform.CLOUDBEES, "http://localhost:8080/jenkins/My%20View");
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/My%20View/api/json?tree=name,url,views%5Bjobs%5Bname,fullName,displayName,fullDisplayName,jobs,url,color,buildable,inQueue,healthReport%5Bdescription,iconUrl%5D,lastBuild%5Bid,url,building,result,number,timestamp,duration%5D,property%5BparameterDefinitions%5Bname,type,defaultParameterValue%5Bvalue%5D,description,choices%5D%5D%5D%5D"));
    }

    @Test
    public void createRssLastBuildUrl() throws Exception {
        URL url = urlBuilder.createRssLatestUrl("http://localhost:8080/jenkins");
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/rssLatest"));
    }

    @Test
    public void createAuthenticationJSONUrl() throws Exception {
        URL url = urlBuilder.createAuthenticationUrl("http://localhost:8080/jenkins");
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/api/json?tree=nodeName"));
    }

    @Before
    public void setUp() {
        configuration = new JenkinsAppSettings();
        urlBuilder = new UrlBuilder();
    }


}
