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

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.model.VirtualFilePartSource;
import org.codinjutsu.tools.jenkins.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class DefaultSecurityClient implements SecurityClient {

    private static final String BAD_CRUMB_DATA = "No valid crumb was included in the request";
    private static final int DEFAULT_SOCKET_TIMEOUT = 10000;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;

    protected String crumbData;
    protected JenkinsVersion jenkinsVersion = JenkinsVersion.VERSION_1;

    protected final HttpClient httpClient;
    protected Map<String, VirtualFile> files = new HashMap<String, VirtualFile>();

    DefaultSecurityClient(String crumbData) {
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        this.crumbData = crumbData;
    }

    @Override
    public void connect(URL jenkinsUrl) {
        execute(jenkinsUrl);
    }

    public String execute(URL url) {
        String urlStr = url.toString();

        ResponseCollector responseCollector = new ResponseCollector();
        runMethod(urlStr, responseCollector);

        if (isRedirection(responseCollector.statusCode)) {
            runMethod(responseCollector.data, responseCollector);
        }

        return responseCollector.data;
    }

    @Override
    public void setFiles(Map<String, VirtualFile> files) {
        this.files = files;
    }

    private PostMethod addFiles(PostMethod post) {
        if (files.size() > 0) {
            ArrayList<Part> parts = new ArrayList<Part>();
            int i = 0;
            for(String key: files.keySet()) {
                VirtualFile virtualFile = files.get(key);
                parts.add(new StringPart("name", key));
                parts.add(new StringPart("json", "{\"parameter\":{\"name\":\"" + key + "\",\"file\":\""+ String.format("file%d", i) +"\"}}"));
                parts.add(new FilePart(String.format("file%d", i), new VirtualFilePartSource(virtualFile)));
                i++;
            }
            post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), post.getParams()));
            files.clear();
        }

        return post;
    }

    private void runMethod(String url, ResponseCollector responseCollector) {
        PostMethod post = new PostMethod(url);

        if (isCrumbDataSet()) {
            post.addRequestHeader(jenkinsVersion.getCrumbName(), crumbData);
        }

        post = addFiles(post);


        try {
            if (files.isEmpty()) {
                httpClient.getParams().setParameter("http.socket.timeout", DEFAULT_SOCKET_TIMEOUT);
                httpClient.getParams().setParameter("http.connection.timeout", DEFAULT_CONNECTION_TIMEOUT);
            } else {
                httpClient.getParams().setParameter("http.socket.timeout", 0);
                httpClient.getParams().setParameter("http.connection.timeout", 0);
            }

            int statusCode = httpClient.executeMethod(post);
            final String responseBody;
            try(InputStream inputStream = post.getResponseBodyAsStream()) {
                responseBody = IOUtils.toString(inputStream, post.getResponseCharSet());
            }
            checkResponse(statusCode, responseBody);

            if (HttpURLConnection.HTTP_OK == statusCode) {
                responseCollector.collect(statusCode, responseBody);
            }
            if (isRedirection(statusCode)) {
                responseCollector.collect(statusCode, post.getResponseHeader("Location").getValue());
            }
        } catch (HttpException httpEx) {
            throw new ConfigurationException(String.format("HTTP Error during method execution '%s': %s", url, httpEx.getMessage()), httpEx);
        } catch (UnknownHostException uhEx) {
            throw new ConfigurationException(String.format("Unknown server: %s", uhEx.getMessage()), uhEx);
        } catch (IOException ioEx) {
            throw new ConfigurationException(String.format("IO Error during method execution '%s': %s", url, ioEx.getMessage()), ioEx);
        } finally {
            post.releaseConnection();
        }
    }

    protected void checkResponse(int statusCode, String responseBody) throws AuthenticationException {
        if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new AuthenticationException("Not found");
        }

        if (statusCode == HttpURLConnection.HTTP_FORBIDDEN || statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (StringUtils.containsIgnoreCase(responseBody, BAD_CRUMB_DATA)) {
                throw new AuthenticationException("CSRF enabled -> Missing or bad crumb data");
            }

            throw new AuthenticationException("Unauthorized -> Missing or bad credentials", responseBody);
        }

        if (HttpURLConnection.HTTP_INTERNAL_ERROR == statusCode) {
            throw new AuthenticationException("Server Internal Error: Server unavailable");
        }
    }

    private boolean isRedirection(int statusCode) {
        return statusCode / 100 == 3;
    }


    protected boolean isCrumbDataSet() {
        return StringUtils.isNotBlank(crumbData);
    }

    private static class ResponseCollector {

        private int statusCode;
        private String data;

        void collect(int statusCode, String body) {
            this.statusCode = statusCode;
            this.data = body;
        }

    }

    public void setJenkinsVersion(JenkinsVersion jenkinsVersion) {
        this.jenkinsVersion = jenkinsVersion;
    }
}
