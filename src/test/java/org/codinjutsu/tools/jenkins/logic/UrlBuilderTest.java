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

import static org.assertj.core.api.Assertions.*;

public class UrlBuilderTest {
    private JenkinsAppSettings configuration;

    private UrlBuilder urlBuilder;


    @Test
    public void createRunJobUrl() {
        configuration.setDelay(20);

        URL url = urlBuilder.createRunJobUrl("http://localhost:8080/jenkins/My%20Job", configuration);
        assertThat(url.toString()).isEqualTo("http://localhost:8080/jenkins/My%20Job/build?delay=20sec");
    }

    @Test
    public void createRunParameterizedJobUrl() {
        configuration.setDelay(20);

        Map<String, String> valueByNameParams = new LinkedHashMap<>();
        valueByNameParams.put("param1", "value1");
        valueByNameParams.put("param2", "value2");

        URL url = urlBuilder.createRunParameterizedJobUrl("http://localhost:8080/jenkins/My%20Job", configuration, valueByNameParams);
        assertThat(url.toString()).isEqualTo("http://localhost:8080/jenkins/My%20Job/buildWithParameters?delay=20sec&param1=value1&param2=value2");
    }

    @Test
    public void createJenkinsWorkspaceUrl() {
        configuration.setServerUrl("http://localhost:8080/jenkins");

        URL url = urlBuilder.createJenkinsWorkspaceUrl(configuration);
        assertThat(url.toString()).isEqualTo("http://localhost:8080/jenkins/api/json?tree=url,description,nodeName,nodeDescription,primaryView%5Bname,url%5D,views%5Bname,url,views%5Bname,url%5D%5D");
    }

    @Test
    public void createViewUrlForClassicPlateform() {
        URL url = urlBuilder.createViewUrl(JenkinsPlateform.CLASSIC, "http://localhost:8080/jenkins/My%20View");
        assertThat(url.toString()).isEqualTo("http://localhost:8080/jenkins/My%20View/api/json?tree=name,url,jobs%5Bname,fullName,displayName,fullDisplayName,jobs,url,color,buildable,inQueue,healthReport%5Bdescription,iconUrl%5D,lastBuild%5Bid,url,building,result,number,displayName,fullDisplayName,timestamp,duration%5D,property%5BparameterDefinitions%5Bname,type,defaultParameterValue%5Bvalue%5D,description,choices%5D%5D%5D");
    }

    @Test
    public void createJobJSONUrl() {
        URL url = urlBuilder.createJobUrl("http://localhost:8080/jenkins/my%20Job");
        assertThat(url.toString()).isEqualTo("http://localhost:8080/jenkins/my%20Job/api/json?tree=name,fullName,displayName,fullDisplayName,jobs,url,color,buildable,inQueue,healthReport%5Bdescription,iconUrl%5D,lastBuild%5Bid,url,building,result,number,displayName,fullDisplayName,timestamp,duration%5D,property%5BparameterDefinitions%5Bname,type,defaultParameterValue%5Bvalue%5D,description,choices%5D%5D");
    }

    @Test
    public void createViewUrlForCloudbeesPlateform() {
        URL url = urlBuilder.createViewUrl(JenkinsPlateform.CLOUDBEES, "http://localhost:8080/jenkins/My%20View");
        assertThat(url.toString()).isEqualTo("http://localhost:8080/jenkins/My%20View/api/json?tree=name,url,views%5Bjobs%5Bname,fullName,displayName,fullDisplayName,jobs,url,color,buildable,inQueue,healthReport%5Bdescription,iconUrl%5D,lastBuild%5Bid,url,building,result,number,displayName,fullDisplayName,timestamp,duration%5D,property%5BparameterDefinitions%5Bname,type,defaultParameterValue%5Bvalue%5D,description,choices%5D%5D%5D%5D");
    }

    @Test
    public void createRssLastBuildUrl() {
        URL url = urlBuilder.createRssLatestUrl("http://localhost:8080/jenkins");
        assertThat(url.toString()).isEqualTo("http://localhost:8080/jenkins/rssLatest");
    }

    @Test
    public void createAuthenticationJSONUrl() {
        URL url = urlBuilder.createAuthenticationUrl("http://localhost:8080/jenkins");
        assertThat(url.toString()).isEqualTo("http://localhost:8080/jenkins/api/json?tree=nodeName");
    }

    @Test
    public void createComputerJSONUrl() {
        URL url = urlBuilder.createComputerUrl("http://localhost:8080/jenkins");
        assertThat(url.toString()).isEqualTo("http://localhost:8080/jenkins/computer/api/json?tree=computer%5BdisplayName,description,offline,assignedLabels%5Bname%5D%5D");
    }

    @Before
    public void setUp() {
        configuration = new JenkinsAppSettings();
        urlBuilder = new UrlBuilder();
    }


    @Test
    public void getBaseUrl() {
        assertThat(UrlBuilder.getBaseUrl("http://localhost:8080")).isEqualTo("http://localhost:8080");
        assertThat(UrlBuilder.getBaseUrl("http://localhost:8080/job/project")).isEqualTo("http://localhost:8080");
        assertThat(UrlBuilder.getBaseUrl("http://localhost:8080/view/view-name/job/project")).isEqualTo("http://localhost:8080");
    }

}
