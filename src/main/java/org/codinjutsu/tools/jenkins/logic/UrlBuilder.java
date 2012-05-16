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

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

class UrlBuilder {

    private static final String API_XML = "/api/xml";
    private static final String BUILD = "/build";
    private static final String PARAMETERIZED_BUILD = "/buildWithParameters";
    private static final String RSS_LATEST = "/rssLatest";
    private static final String TREE_PARAM = "?tree=";
    private static final String BASIC_JENKINS_INFO = "nodeName,nodeDescription,primaryView[name,url],views[name,url,views[name,url]]";
    private static final String BASIC_JOB_INFO = "name,url,color,buildable,inQueue,healthReport[description,iconUrl],lastBuild[id,url,building,result,number],property[parameterDefinitions[name,type,defaultParameterValue[value],choices]]";
    private static final String BASIC_VIEW_INFO = "name,url,jobs[" + BASIC_JOB_INFO + "]";
    public static final String TEST_CONNECTION_REQUEST = "/api/xml?tree=nodeName";

    public URL createRunJobUrl(String jobBuildUrl, JenkinsConfiguration configuration) {
        try {
            return new URL(jobBuildUrl + URIUtil.encodePathQuery(String.format("%s?delay=%dsec",BUILD, configuration.getBuildDelay())));
        } catch (MalformedURLException malformedURLEx) {
            throw new IllegalArgumentException("URL is malformed", malformedURLEx);
        } catch (URIException uriEx) {
            throw new IllegalArgumentException("Error during URL creation", uriEx);
        }
    }

    public URL createRunParameterizedJobUrl(String jobUrl, JenkinsConfiguration configuration, Map<String, String> paramValueMap) {
        StringBuilder strBuilder = new StringBuilder(String.format("%s?delay=%dsec",PARAMETERIZED_BUILD, configuration.getBuildDelay()));
        for (Map.Entry<String, String> valueByName : paramValueMap.entrySet()) {
            strBuilder.append("&").append(valueByName.getKey()).append("=").append(valueByName.getValue());
        }
        try {
            return new URL(jobUrl + URIUtil.encodePathQuery(strBuilder.toString()));
        } catch (MalformedURLException malformedURLEx) {
            throw new IllegalArgumentException("URL is malformed", malformedURLEx);
        } catch (URIException uriEx) {
            throw new IllegalArgumentException("Error during URL creation", uriEx);
        }
    }

    public URL createJenkinsWorkspaceUrl(JenkinsConfiguration configuration) {
        try {
            return new URL(URIUtil.encodePathQuery(configuration.getServerUrl() + API_XML + TREE_PARAM + BASIC_JENKINS_INFO));
        } catch (MalformedURLException malformedURLEx) {
            throw new IllegalArgumentException("URL is malformed", malformedURLEx);
        } catch (URIException uriEx) {
            throw new IllegalArgumentException("Error during URL creation", uriEx);
        }
    }

    public URL createViewUrl(String viewUrl) {
        try {
            return new URL(viewUrl + URIUtil.encodePathQuery(API_XML + TREE_PARAM + BASIC_VIEW_INFO));
        } catch (MalformedURLException malformedURLEx) {
            throw new IllegalArgumentException("URL is malformed", malformedURLEx);
        } catch (URIException uriEx) {
            throw new IllegalArgumentException("Error during URL creation", uriEx);
        }
    }

    public URL createJobUrl(String jobUrl) {
        try {
            return new URL(jobUrl + URIUtil.encodePathQuery(API_XML + TREE_PARAM + BASIC_JOB_INFO));
        } catch (MalformedURLException malformedURLEx) {
            throw new IllegalArgumentException("URL is malformed", malformedURLEx);
        } catch (URIException uriEx) {
            throw new IllegalArgumentException("Error during URL creation", uriEx);
        }
    }

    public URL createRssLatestUrl(String serverUrl) {
        try {
            return new URL(serverUrl + RSS_LATEST);
        } catch (MalformedURLException malformedURLEx) {
            throw new IllegalArgumentException("URL is malformed", malformedURLEx);
        }
    }

    public URL createAuthenticationUrl(String serverUrl) {
        try {
            return new URL(serverUrl + TEST_CONNECTION_REQUEST);
        } catch (MalformedURLException malformedUrlEx) {
            throw new IllegalArgumentException("URL is malformed", malformedUrlEx);
        }
    }
}
