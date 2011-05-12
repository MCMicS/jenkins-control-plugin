package org.codinjutsu.tools.jenkins.logic;

import org.codinjutsu.tools.jenkins.JenkinsConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

class UrlBuilder {

    private static final String API_XML = "/api/xml";
    private static final String LAST_BUILD = "/lastBuild";
    private static final String BUILD = "/build";
    private static final String RSS_LATEST = "/rssLatest";
    private static final String TREE_PARAM = "?tree=";
    private static final String BASIC_JENKINS_INFO = "nodeName,nodeDescription,primaryView[name,url],views[name,url]";
    private static final String BASIC_JOB_INFO = "name,url,color,inQueue,lastBuild[url,building,result,number]";
    private static final String BASIC_VIEW_INFO = "name,url,jobs[" + BASIC_JOB_INFO + "]";
    private static final String URL_SPACE_CHAR = "%20";
    private static final String SPACE_CHAR = " ";

    public URL createRunJobUrl(String jobBuildUrl, JenkinsConfiguration configuration)
            throws MalformedURLException {
        return new URL(formatToReadableURL(
                jobBuildUrl + BUILD + "?delay=" + configuration.getBuildDelay() + "sec"));
    }

    public URL createJenkinsWorkspaceUrl(JenkinsConfiguration configuration)
            throws MalformedURLException {
        return new URL(configuration.getServerUrl() + API_XML + TREE_PARAM + BASIC_JENKINS_INFO);
    }

    public URL createViewUrl(String viewUrl) throws MalformedURLException {
        return new URL(formatToReadableURL(viewUrl + API_XML + TREE_PARAM + BASIC_VIEW_INFO));
    }

    public URL createJobUrl(String jobUrl) throws MalformedURLException {
        return new URL(formatToReadableURL(jobUrl + API_XML + TREE_PARAM + BASIC_JOB_INFO));
    }

    public URL createRssLatestUrl(String serverUrl) throws MalformedURLException {
        return new URL(serverUrl + RSS_LATEST);
    }

    private static String formatToReadableURL(String url) {
        return url.replace(SPACE_CHAR, URL_SPACE_CHAR);
    }
}
