package org.codinjutsu.tools.jenkins.logic;

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
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/api/xml?tree=nodeName,nodeDescription,primaryView[name,url],views[name,url]"));
    }


    @Test
    public void createViewUrl() throws Exception {
        URL url = urlBuilder.createViewUrl("http://localhost:8080/jenkins/My View");
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/My%20View/api/xml?tree=name,url,jobs[name,url,color,inQueue,lastBuild[url,building,result,number]]"));
    }


    @Test
    public void createJobUrl() throws Exception {
        URL url = urlBuilder.createJobUrl("http://localhost:8080/jenkins/my Job");
        assertThat(url.toString(), equalTo("http://localhost:8080/jenkins/my%20Job/api/xml?tree=name,url,color,inQueue,lastBuild[url,building,result,number]"));
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
