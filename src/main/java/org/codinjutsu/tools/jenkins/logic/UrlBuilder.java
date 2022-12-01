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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import lombok.SneakyThrows;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.view.action.UploadPatchToJobAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class UrlBuilder {

    private static final Logger LOG = Logger.getInstance(UploadPatchToJobAction.class.getName());

    private static final String API_JSON = "/api/json";
    private static final String BUILD = "/build";
    private static final String RSS_LATEST = "/rssLatest";
    private static final String TREE_PARAM = "?tree=";
    private static final String URL = "url";
    private static final String BASIC_JENKINS_INFO = URL + ",description,nodeName,nodeDescription,primaryView[name,url],views[name,url,views[name,url]]";
    private static final String BASIC_BUILD_INFO = URL + ",id,building,result,number,displayName,fullDisplayName," +
            "timestamp,duration,actions[parameters[name,value]]";
    private static final String BASIC_JOB_INFO = "name,fullName,displayName,fullDisplayName,jobs," + URL + ",color,buildable,inQueue,healthReport[description,iconUrl],lastBuild[" + BASIC_BUILD_INFO + "],lastFailedBuild[" + URL + "],lastSuccessfulBuild[" + URL + "],property[parameterDefinitions[name,type,defaultParameterValue[value],description,choices]]";
    private static final String BASIC_VIEW_INFO = "name," + URL + ",jobs[" + BASIC_JOB_INFO + "]";
    private static final String CLOUDBEES_VIEW_INFO = "name," + URL + ",views[jobs[" + BASIC_JOB_INFO + "]]";
    private static final String TEST_CONNECTION_REQUEST = "?tree=nodeName,url,description,primaryView[name,url]";
    private static final String BASIC_BUILDS_INFO = "builds[" + BASIC_BUILD_INFO + "]";
    private static final String NESTED_JOBS_INFO =  URL + "name,displayName,fullDisplayName,jobs[" + BASIC_JOB_INFO + "]";
    private static final String COMPUTER = "/computer";
    private static final String COMPUTER_INFO = "computer[displayName,description,offline,assignedLabels[name]]";
    /**
     * in git-parameter-plugin
     * also field 'allValueItems' could be used: property[parameterDefinitions[name,type,defaultParameterValue[value],description,allValueItems]]
     */
    private static final String FILL_VALUE_ITEMS = "descriptorByName/%s/fillValueItems?param=%s";
    private static final String ERROR_DURING_URL_CREATION = "Error during URL creation";

    public static UrlBuilder getInstance(Project project) {
        return Optional.ofNullable(project.getService(UrlBuilder.class))
                .orElseGet(UrlBuilder::new);
    }

    @NotNull
    static String getBaseUrl(@NotNull String url) {
        return url.replaceAll("\\b/job\\b/.+|/\\bview\\b/.+", "");
    }

    public URL createRunJobUrl(String jobBuildUrl, JenkinsAppSettings configuration) {
        try {
            String s = jobBuildUrl + encodePathQuery(String.format("%s?delay=%dsec", BUILD, configuration.getBuildDelay()));
            return new URL(s);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    @SneakyThrows
    private static String encodePathQuery(String unescaped) {
        final var queryIndex = unescaped.indexOf('?');
        if (queryIndex == -1) {
            return encodePath(unescaped);
        } else {
            var path = encodePath(unescaped.substring(0, queryIndex));
            var query = unescaped.substring(queryIndex + 1);
            try {
                query = new URI(null, null, null, query, null)
                        .toASCIIString().substring(1);  // remove leading ?
            }
            catch (URISyntaxException e) {
                LOG.debug(e.getMessage(), e);
            }
            //return com.intellij.util.io.URLUtil.encodeQuery(query);
            return path + '?' + query;
        }
    }

    @SneakyThrows
    private static String encodePath(String unescaped) {
        try {
            return new URI(null, null, unescaped, null, null).toASCIIString();
        }
        catch (URISyntaxException e) {
            return unescaped;
        }
        //return com.intellij.util.io.URLUtil.encodePath(unescaped);
    }

    public URL createStopBuildUrl(String buildUrl) {
        try {//http://jenkins.internal/job/it4em-it4em-DPD-GEOR-UAT-RO/27/stop
            return new URL(buildUrl + encodePath("stop"));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createJenkinsWorkspaceUrl(JenkinsAppSettings configuration) {
        try {
            return new URL(encodePathQuery(configuration.getServerUrl() + API_JSON + TREE_PARAM + BASIC_JENKINS_INFO));
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
            return new URL(viewUrl + encodePathQuery(API_JSON + TREE_PARAM + basicViewInfo));
        } catch (Exception ex) {
            handleException(ex);
        }

        return null;
    }

    public URL createJobUrl(String jobUrl) {
        try {
            return new URL(jobUrl + encodePathQuery(API_JSON + TREE_PARAM + BASIC_JOB_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createBuildUrl(String buildUrl) {
        try {
            return new URL(buildUrl + encodePathQuery(API_JSON + TREE_PARAM + BASIC_BUILD_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    public URL createBuildsUrl(String buildUrl) {
        try {
            return new URL(buildUrl + encodePathQuery(API_JSON + TREE_PARAM + BASIC_BUILDS_INFO));
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
            return new URL(serverUrl + encodePathQuery(API_JSON + TEST_CONNECTION_REQUEST));
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
        }
//        else if (ex instanceof URIException) {
//            throw new IllegalArgumentException(ERROR_DURING_URL_CREATION, ex);
//        }
    }

    public URL createNestedJobUrl(String currentJobUrl) {
        try {
            return new URL(currentJobUrl + encodePathQuery(API_JSON + TREE_PARAM + NESTED_JOBS_INFO));
        } catch (Exception ex) {
            handleException(ex);
        }

        return null;
    }

    @NotNull
    public URL createComputerUrl(String serverUrl) {
        try {
            return new URL(serverUrl + encodePathQuery(COMPUTER + API_JSON + TREE_PARAM +
                    COMPUTER_INFO));
        } catch (Exception ex) {
            handleException(ex);
            throw new IllegalArgumentException(ERROR_DURING_URL_CREATION, ex);
        }
    }

    @NotNull
    public URL createConfigureUrl(@NotNull String serverUrl) {
        try {
            return new URL(removeTrailingSlash(serverUrl) + "/configure");
        } catch (Exception ex) {
            handleException(ex);
            throw new IllegalArgumentException(ERROR_DURING_URL_CREATION, ex);
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

    public URL createFillValueItemsUrl(String jobUrl, String className, String param) {
        try {
            return new URL(jobUrl +  encodePathQuery(String.format(FILL_VALUE_ITEMS, className, param)));
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }
}
