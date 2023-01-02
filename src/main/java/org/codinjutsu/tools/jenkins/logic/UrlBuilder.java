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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.UriUtil;
import lombok.SneakyThrows;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class UrlBuilder {

    private static final Logger LOG = Logger.getInstance(UrlBuilder.class.getName());

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
        return buildUrl(jobBuildUrl, encodePathQuery(String.format("%s?delay=%dsec", BUILD, configuration.getBuildDelay())));
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
        //http://jenkins.internal/job/it4em-it4em-DPD-GEOR-UAT-RO/27/stop
        return buildUrl(buildUrl, encodePath("stop"));
    }

    public URL createJenkinsWorkspaceUrl(JenkinsAppSettings configuration) {
        return buildUrl(configuration.getServerUrl(), encodePathQuery(API_JSON + TREE_PARAM + BASIC_JENKINS_INFO));
    }

    public URL createViewUrl(JenkinsPlateform jenkinsPlateform, String viewUrl) {
        String basicViewInfo = BASIC_VIEW_INFO;
        if (JenkinsPlateform.CLOUDBEES.equals(jenkinsPlateform)) {
            basicViewInfo = CLOUDBEES_VIEW_INFO;
        }
        return buildUrl(viewUrl, encodePathQuery(API_JSON + TREE_PARAM + basicViewInfo));
    }

    public URL createJobUrl(String jobUrl) {
        return buildUrl(jobUrl, encodePathQuery(API_JSON + TREE_PARAM + BASIC_JOB_INFO));
    }

    public URL createBuildUrl(String buildUrl) {
        return buildUrl(buildUrl, encodePathQuery(API_JSON + TREE_PARAM + BASIC_BUILD_INFO));
    }

    public URL createBuildsUrl(String buildUrl) {
        return buildUrl(buildUrl, encodePathQuery(API_JSON + TREE_PARAM + BASIC_BUILDS_INFO));
    }

    public URL createRssLatestUrl(String serverUrl) {
        return buildUrl(serverUrl, RSS_LATEST);
    }

    public URL createAuthenticationUrl(String serverUrl) {
        return buildUrl(serverUrl, encodePathQuery(API_JSON + TEST_CONNECTION_REQUEST));
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
        LOG.debug(ex);
        if (ex instanceof MalformedURLException) {
            throw new IllegalArgumentException("URL is malformed", ex);
        }
//        else if (ex instanceof URIException) {
//            throw new IllegalArgumentException(ERROR_DURING_URL_CREATION, ex);
//        }
    }

    public URL createNestedJobUrl(String currentJobUrl) {
        return buildUrl(currentJobUrl, encodePathQuery(API_JSON + TREE_PARAM + NESTED_JOBS_INFO));
    }

    @NotNull
    public URL createComputerUrl(String serverUrl) {
        try {
            return buildUrlNotNull(serverUrl, encodePathQuery(COMPUTER + API_JSON + TREE_PARAM +
                    COMPUTER_INFO));
        } catch (Exception ex) {
            handleException(ex);
            throw new IllegalArgumentException(ERROR_DURING_URL_CREATION, ex);
        }
    }

    @NotNull
    public URL createConfigureUrl(@NotNull String serverUrl) {
        try {
            return buildUrlNotNull(removeTrailingSlash(serverUrl), "/configure");
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
        return buildUrl(jobUrl, encodePathQuery(String.format(FILL_VALUE_ITEMS, className, param)));
    }

    private @Nullable URL buildUrl(String context, String pathWithQuery) {
        try {
            return buildUrlNotNull(context, pathWithQuery);
        } catch (Exception ex) {
            handleException(ex);
        }
        return null;
    }

    private @NotNull URL buildUrlNotNull(String context, @NotNull String pathWithQuery) throws MalformedURLException {
        final boolean pathWithLeadingSlash = StringUtil.startsWithChar(pathWithQuery, '/');
        final String serverContext = pathWithLeadingSlash ? UriUtil.trimTrailingSlashes(context) : pathWithQuery;
        return new URL(serverContext + pathWithQuery);
    }
}
