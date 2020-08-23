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

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

public class UrlBuilder {

    private static final String API_JSON = "/api/json";
    private static final String BUILD = "/build";
    private static final String PARAMETERIZED_BUILD = "/buildWithParameters";
    private static final String RSS_LATEST = "/rssLatest";
    private static final String TREE_PARAM = "?tree=";
    private static final String URL = "url";
    private static final String BASIC_JENKINS_INFO = URL + ",description,nodeName,nodeDescription,primaryView[name,url],views[name,url,views[name,url]]";
    private static final String BASIC_BUILD_INFO = URL + ",id,building,result,number,displayName,fullDisplayName,timestamp,duration";
    private static final String BASIC_JOB_INFO = "name,fullName,displayName,fullDisplayName,jobs," + URL + ",color,buildable,inQueue,healthReport[description,iconUrl],lastBuild[" + BASIC_BUILD_INFO + "],lastFailedBuild[" + URL + "],lastSuccessfulBuild[" + URL + "],property[parameterDefinitions[name,type,defaultParameterValue[value],description,choices]]";
    private static final String BASIC_VIEW_INFO = "name," + URL + ",jobs[" + BASIC_JOB_INFO + "]";
    private static final String CLOUDBEES_VIEW_INFO = "name," + URL + ",views[jobs[" + BASIC_JOB_INFO + "]]";
    private static final String TEST_CONNECTION_REQUEST = "?tree=nodeName,url,description,primaryView[name,url]";
    private static final String BASIC_BUILDS_INFO = "builds[" + BASIC_BUILD_INFO + "]";
    private static final String NESTED_JOBS_INFO =  URL + "name,displayName,fullDisplayName,jobs[" + BASIC_JOB_INFO + "]";
    private static final String COMPUTER = "/computer";
    private static final String COMPUTER_INFO = "computer[displayName,description,offline,assignedLabels[name]]";

    public static UrlBuilder getInstance(Project project) {
        return Optional.ofNullable(ServiceManager.getService(project, UrlBuilder.class))
                .orElseGet(UrlBuilder::new);
    }

    @NotNull
    static String getBaseUrl(@NotNull String url) {
        return url.replaceAll("\\b/job\\b/.+|/\\bview\\b/.+", "");
    }

    public URL createRunJobUrl(String jobBuildUrl, JenkinsAppSettings configuration) {
        try {
            String s = jobBuildUrl + URIUtil.encodePathQuery(String.format("%s?delay=%dsec", BUILD, configuration.getBuildDelay()));
            return new URL(s);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createStopBuildUrl(String buildUrl) {
        try {//http://jenkins.internal/job/it4em-it4em-DPD-GEOR-UAT-RO/27/stop
            return new URL(buildUrl + URIUtil.encodePath("stop"));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createRunParameterizedJobUrl(String jobUrl, JenkinsAppSettings configuration, Map<String, String> paramValueMap) {
        StringBuilder strBuilder = new StringBuilder(String.format("%s?delay=%dsec", PARAMETERIZED_BUILD, configuration.getBuildDelay()));
        paramValueMap.forEach((name, value) -> strBuilder.append("&").append(name).append("=").append(value));
        try {
            return new URL(jobUrl + URIUtil.encodePathQuery(strBuilder.toString()));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createJenkinsWorkspaceUrl(JenkinsAppSettings configuration) {
        try {
            return new URL(URIUtil.encodePathQuery(configuration.getServerUrl() + API_JSON + TREE_PARAM + BASIC_JENKINS_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createViewUrl(JenkinsPlateform jenkinsPlateform, String viewUrl) {
        String basicViewInfo = BASIC_VIEW_INFO;
        if (JenkinsPlateform.CLOUDBEES.equals(jenkinsPlateform)) {
            basicViewInfo = CLOUDBEES_VIEW_INFO;
        }
        try {
            return new URL(viewUrl + URIUtil.encodePathQuery(API_JSON + TREE_PARAM + basicViewInfo));
        } catch (Exception ex) {
            handleException(ex);
        }

        return null;
    }

    public URL createJobUrl(String jobUrl) {
        try {
            return new URL(jobUrl + URIUtil.encodePathQuery(API_JSON + TREE_PARAM + BASIC_JOB_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createBuildUrl(String buildUrl) {
        try {
            return new URL(buildUrl + URIUtil.encodePathQuery(API_JSON + TREE_PARAM + BASIC_BUILD_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createBuildsUrl(String buildUrl) {
        try {
            return new URL(buildUrl + URIUtil.encodePathQuery(API_JSON + TREE_PARAM + BASIC_BUILDS_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createRssLatestUrl(String serverUrl) {
        try {
            return new URL(serverUrl + RSS_LATEST);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createAuthenticationUrl(String serverUrl) {
        try {
            return new URL(serverUrl + URIUtil.encodePathQuery(API_JSON + TEST_CONNECTION_REQUEST));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URI createServerUrl(String serverUrl) {
        try {
            return new URL(getBaseUrl(serverUrl)).toURI();
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    private void handleException(Exception ex) {
        if (ex instanceof MalformedURLException) {
            throw new IllegalArgumentException("URL is malformed", ex);
        } else if (ex instanceof URIException) {
            throw new IllegalArgumentException("Error during URL creation", ex);
        }
    }

    public URL createNestedJobUrl(String currentJobUrl) {
        try {
            return new URL(currentJobUrl + URIUtil.encodePathQuery(API_JSON + TREE_PARAM + NESTED_JOBS_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }

        return null;
    }

    @NotNull
    public URL createComputerUrl(String serverUrl) {
        try {
            return new URL(serverUrl + URIUtil.encodePathQuery(COMPUTER + API_JSON + TREE_PARAM +
                    COMPUTER_INFO));
        } catch (Exception ex) {
            handleException(ex);
            throw new IllegalArgumentException("Error during URL creation", ex);
        }
    }

    @NotNull
    public URL createConfigureUrl(@NotNull String serverUrl) {
        try {
            return new URL(removeTrailingSlash(serverUrl) + "/configure");
        } catch (Exception ex) {
            handleException(ex);
            throw new IllegalArgumentException("Error during URL creation", ex);
        }
    }

    @Nullable
    public URL toUrl(@NotNull String url) {
        try {
            return new URL(url);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @NotNull
    public String removeTrailingSlash(@NotNull String url) {
        final String withoutTrailingSlash;
        if (url.endsWith("/")) {
            withoutTrailingSlash = url.substring(0, url.length() - 1);
        } else {
            withoutTrailingSlash = url;
        }
        return withoutTrailingSlash;
    }
}
