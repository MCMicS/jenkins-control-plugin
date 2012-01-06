/*
 * Copyright (c) 2011 David Boissier
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
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


class BasicSecurityClient extends AbstractSecurityClient {

    private final String username;
    private final String passwordFile;
    private String password = null;


    BasicSecurityClient(String username, String passwordFile, String crumbDataFile) {
        super(new HttpClient(), crumbDataFile);
        this.username = username;
        this.passwordFile = passwordFile;
    }


    public void connect(URL jenkinsURL) throws Exception {
        URL url = new URL(jenkinsURL.toString() + TEST_CONNECTION_REQUEST);

        setCrumbValueIfNeeded();

        if (StringUtils.isNotEmpty(passwordFile)) {
            password = extractValueFromFile(passwordFile);
        }

        doAuthentication(url);
    }

    private void doAuthentication(URL jenkinsUrl) throws IOException, AuthenticationException {

        if (username != null && password != null) {
            httpClient.getState().setCredentials(
                    new AuthScope(jenkinsUrl.getHost(), jenkinsUrl.getPort()),
                    new UsernamePasswordCredentials(username, password));
        }


        httpClient.getParams().setAuthenticationPreemptive(true);

        PostMethod postMethod = new PostMethod(jenkinsUrl.toString());

        if (isCrumbDataSet()) {
            postMethod.addRequestHeader(CRUMB_NAME, crumbValue);
        }

        postMethod.setDoAuthentication(true);
        int responseCode = httpClient.executeMethod(postMethod);
        if (responseCode != HttpURLConnection.HTTP_OK) {
            checkResponse(responseCode, postMethod.getResponseBodyAsString());
        }

        postMethod.releaseConnection();
    }


    public String execute(URL url) throws Exception {
        String urlStr = url.toString();
        PostMethod post = new PostMethod(urlStr);
        setCrumbValueIfNeeded();

        if (isCrumbDataSet()) {
            post.addRequestHeader(CRUMB_NAME, crumbValue);
        }

        try {
            int statusCode = httpClient.executeMethod(post);
            String responseBody = post.getResponseBodyAsString();
            if (HttpURLConnection.HTTP_OK != statusCode) {//TODO Crappy ! need refactor
                if (isRedirection(statusCode)) {
                    String newLocation = post.getResponseHeader("Location").getValue();
                    post = new PostMethod(newLocation);
                    setCrumbValueIfNeeded();

                    if (isCrumbDataSet()) {
                        post.addRequestHeader(CRUMB_NAME, crumbValue);
                    }

                    statusCode = httpClient.executeMethod(post);
                    responseBody = post.getResponseBodyAsString();
                    if (HttpURLConnection.HTTP_OK != statusCode) {
                        checkResponse(statusCode, responseBody);
                    }
                } else {
                    checkResponse(post.getStatusCode(), responseBody);
                }
            }
            return responseBody;
        } finally {
            post.releaseConnection();
        }
    }
}
