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

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


class BasicSecurityClient extends DefaultSecurityClient {

    private final String username;
    private String password = null;


    BasicSecurityClient(String username, String password, String crumbData) {
        super(crumbData);
        this.username = username;
        this.password = password;
    }


    public void connect(URL url) {
        doAuthentication(url);
    }

    private void doAuthentication(URL jenkinsUrl) throws AuthenticationException {

        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            httpClient.getState().setCredentials(
                    new AuthScope(jenkinsUrl.getHost(), jenkinsUrl.getPort()),
                    new UsernamePasswordCredentials(username, password));
        }


        httpClient.getParams().setAuthenticationPreemptive(true);

        PostMethod post = new PostMethod(jenkinsUrl.toString());
        InputStream inputStream = null;
        try {
            if (isCrumbDataSet()) {
                post.addRequestHeader(CRUMB_NAME, crumbData);
            }

            post.setDoAuthentication(true);
            int responseCode = httpClient.executeMethod(post);
            inputStream = post.getResponseBodyAsStream();
            String responseBody = IOUtils.toString(inputStream, post.getResponseCharSet());
            if (responseCode != HttpURLConnection.HTTP_OK) {
                checkResponse(responseCode, responseBody);
            }
        } catch (HttpException httpEx) {
            throw new ConfigurationException(String.format("HTTP Error during method execution '%s': %s", jenkinsUrl.toString(), httpEx.getMessage()), httpEx);
        } catch (IOException ioEx) {
            throw new ConfigurationException(String.format("IO Error during method execution '%s': %s", jenkinsUrl.toString(), ioEx.getMessage()), ioEx);
        } finally {
            IOUtils.closeQuietly(inputStream);
            post.releaseConnection();
        }
    }
}
