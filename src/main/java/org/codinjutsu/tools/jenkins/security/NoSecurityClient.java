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
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class NoSecurityClient extends AbstractSecurityClient {


    NoSecurityClient(String crumbDataFile) {
        this(crumbDataFile, new HttpClient());
    }


    NoSecurityClient(String crumbDataFile, HttpClient httpClient) {
        super(httpClient, crumbDataFile);
    }


    public void connect(URL jenkinsURL) throws Exception {
        try {
            URL url = new URL(jenkinsURL.toString() + TEST_CONNECTION_REQUEST);
            execute(url);
        } catch (IOException e) {
            throw new AuthenticationException("Failed to connect to " + jenkinsURL, e);
        }
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
            if (HttpURLConnection.HTTP_OK != statusCode) {
                checkResponse(statusCode, responseBody);
            }
            return responseBody;
        } finally {
            post.releaseConnection();
        }
    }


}
