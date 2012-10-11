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

package org.codinjutsu.tools.jenkins.security;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

class DefaultSecurityClient implements SecurityClient {

    private static final String BAD_CRUMB_DATA = "No valid crumb was included in the request";
    static final String CRUMB_NAME = ".crumb";

    private final String crumbDataFile;
    String crumbValue;

    protected final HttpClient httpClient;

    DefaultSecurityClient(String crumbDataFile) {
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        this.crumbDataFile = crumbDataFile;
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


    private void runMethod(String url, ResponseCollector responseCollector) {
        PostMethod post = new PostMethod(url);
        setCrumbValueIfNeeded();

        if (isCrumbDataSet()) {
            post.addRequestHeader(CRUMB_NAME, crumbValue);
        }

        InputStream inputStream = null;
        try {
            int statusCode = httpClient.executeMethod(post);

            inputStream = post.getResponseBodyAsStream();
            String responseBody = IOUtils.toString(inputStream, post.getResponseCharSet());

            checkResponse(statusCode, responseBody);

            if (HttpURLConnection.HTTP_OK == statusCode) {
                responseCollector.collect(statusCode, responseBody);
            }
            if (isRedirection(statusCode)) {
                responseCollector.collect(statusCode, post.getResponseHeader("Location").getValue());
            }
        } catch (HttpException httpEx) {
            throw new ConfigurationException(String.format("Error during method execution %s", url), httpEx);
        } catch (UnknownHostException uhEx) {
            throw new ConfigurationException(String.format("Unknown server: %s", uhEx.getMessage()), uhEx);
        } catch (IOException ioEx) {
            throw new ConfigurationException(String.format("Error during method execution %s", url), ioEx);
        } finally {
            IOUtils.closeQuietly(inputStream);
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

    protected void setCrumbValueIfNeeded() {
        if (!isCrumbDataSet()) {
            if (StringUtils.isNotEmpty(crumbDataFile)) {
                crumbValue = extractValueFromFile(crumbDataFile);
            }
        }
    }

    protected boolean isCrumbDataSet() {
        return crumbValue != null;
    }

    protected static String extractValueFromFile(String file) {
        FileInputStream passwordFileInputStream = null;
        try {
            passwordFileInputStream = new FileInputStream(file);
            String value = IOUtils.toString(passwordFileInputStream);
            if (StringUtils.isNotEmpty(value)) {
                value = StringUtils.remove(value, '\n');
            }
            return value;
        } catch (FileNotFoundException e) {
            throw new ConfigurationException(String.format("File '%s' not found", file));
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to read '%s'", file), e);
        } finally {
            IOUtils.closeQuietly(passwordFileInputStream);
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