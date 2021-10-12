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

package org.codinjutsu.tools.jenkins.security;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.codinjutsu.tools.jenkins.exception.AuthenticationException;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.model.FileParameter;
import org.codinjutsu.tools.jenkins.model.RequestData;
import org.codinjutsu.tools.jenkins.model.VirtualFilePartSource;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.util.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

class DefaultSecurityClient implements SecurityClient {

    private static final String BAD_CRUMB_DATA = "No valid crumb was included in the request";
    static final String CHARSET = StandardCharsets.UTF_8.name();
    protected final HttpClient httpClient;
    private final int connectionTimout;
    protected String crumbData;
    protected JenkinsVersion jenkinsVersion = JenkinsVersion.VERSION_1;

    DefaultSecurityClient(String crumbData, int connectionTimout) {
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        this.crumbData = crumbData;
        this.connectionTimout = connectionTimout;
    }

    @Nullable
    @Override
    public String connect(URL jenkinsUrl) {
        return execute(jenkinsUrl);
    }

    public String execute(URL url, @NotNull Collection<RequestData> data) {
        String urlStr = url.toString();

        ResponseCollector responseCollector = new ResponseCollector();
        runMethod(urlStr, data, responseCollector);

        if (isRedirection(responseCollector.statusCode)) {
            runMethod(responseCollector.data, data, responseCollector);
        }

        return responseCollector.data;
    }

    /**
     * send parameter via Json:<br>
     * <pre>
     * {
     *   "parameter": [
     *     {
     *       "name": "Parameter-Name",
     *       "value": "Parameter-Value"
     *     },
     *     {
     *       "name": "File-Parameter",
     *       "file": "fileName-same-as-in-parts"
     *     }
     *   ]
     * }
     * </pre>
     */
    @NotNull
    PostMethod createPost(String url, @NotNull Collection<RequestData> data) {
        final PostMethod post = new PostMethod(url);
        if (!data.isEmpty()) {
            final ArrayList<Part> parts = new ArrayList<>();
            data.stream().filter(FileParameter.class::isInstance)
                    .map(FileParameter.class::cast)
                    .map(this::createFilePart)
                    .forEach(parts::add);
            final JsonObject parameterJson = new JsonObject();
            parameterJson.put("parameter", data);
            parts.add(new StringPart("json", Jsoner.serialize(parameterJson), DefaultSecurityClient.CHARSET));
            post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[0]), post.getParams()));
        }

        return post;
    }

    @NotNull
    private FilePart createFilePart(FileParameter fileParameter) {
        final String CONTENT_TYPE= null;
        return new FilePart(fileParameter.getFileName(), new VirtualFilePartSource(fileParameter.getFile()),
                CONTENT_TYPE, DefaultSecurityClient.CHARSET);
    }

    private void runMethod(String url, @NotNull Collection<RequestData> data, ResponseCollector responseCollector) {
        final PostMethod post = createPost(url, data);

        if (isCrumbDataSet()) {
            post.addRequestHeader(jenkinsVersion.getCrumbName(), crumbData);
        }
        post.addRequestHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5");
        post.getParams().setContentCharset(StandardCharsets.UTF_8.name());

        try {
            httpClient.getParams().setParameter("http.socket.timeout", connectionTimout);
            httpClient.getParams().setParameter("http.connection.timeout", connectionTimout);

            int statusCode = httpClient.executeMethod(post);
            final String responseBody;
            try (InputStream inputStream = post.getResponseBodyAsStream()) {
                responseBody = IOUtils.toString(inputStream, post.getResponseCharSet());
            }
            checkResponse(statusCode, responseBody);

            if (HttpURLConnection.HTTP_OK == statusCode) {
                responseCollector.collect(statusCode, responseBody);
            }
            if (isRedirection(statusCode)) {
                responseCollector.collect(statusCode, post.getResponseHeader("Location").getValue());
            }
        } catch (UnknownHostException uhEx) {
            throw new ConfigurationException(String.format("Unknown server: %s", uhEx.getMessage()), uhEx);
        } catch (IOException ioEx) {
            throw new ConfigurationException(String.format("IO Error during method execution [%s] for URL '%s'",
                    ioEx.getMessage(), createUrlForNotification(post)), ioEx);
        } finally {
            post.releaseConnection();
        }
    }

    protected void checkResponse(int statusCode, String responseBody) throws AuthenticationException {
        if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new AuthenticationException("Not found", responseBody);
        }

        if (statusCode == HttpURLConnection.HTTP_FORBIDDEN || statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (StringUtils.containsIgnoreCase(responseBody, BAD_CRUMB_DATA)) {
                throw new AuthenticationException("CSRF enabled -> Missing or bad crumb data", responseBody);
            }

            throw new AuthenticationException("Unauthorized -> Missing or bad credentials", responseBody);
        }

        if (HttpURLConnection.HTTP_INTERNAL_ERROR == statusCode) {
            throw new AuthenticationException("Server Internal Error: Server unavailable", responseBody);
        }
    }

    private boolean isRedirection(int statusCode) {
        return statusCode / 100 == 3;
    }


    protected boolean isCrumbDataSet() {
        return StringUtils.isNotBlank(crumbData);
    }

    public void setJenkinsVersion(JenkinsVersion jenkinsVersion) {
        this.jenkinsVersion = jenkinsVersion;
    }

    protected final String createUrlForNotification(@NotNull String url) {
        int startQueryParamtersIndex = url.indexOf('?');
        if (startQueryParamtersIndex != -1) {
            return HtmlUtil.wrapUrl(url.substring(0, startQueryParamtersIndex), url);
        }
        return HtmlUtil.wrapUrl(url, url);
    }

    protected final String createUrlForNotification(HttpMethodBase httpMethod) {
        try {
            return createUrlForNotification(httpMethod.getURI().toString());
        } catch (URIException e) {
            throw new ConfigurationException(String.format("Invalid URI: %s", e.getMessage()), e);
        }
    }

    private static class ResponseCollector {

        private int statusCode;
        private String data;

        void collect(int statusCode, String body) {
            this.statusCode = statusCode;
            this.data = body;
        }

    }
}
