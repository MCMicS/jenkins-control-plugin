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

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.util.EntityUtils;
import org.codinjutsu.tools.jenkins.exception.AuthenticationException;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;


class BasicSecurityClient extends DefaultSecurityClient {

    private static final Logger LOG = Logger.getInstance(BasicSecurityClient.class);

    private final String username;
    private String password = null;


    BasicSecurityClient(String username, String password, String crumbData, int connectionTimout) {
        super(crumbData, connectionTimout);
        this.username = username;
        this.password = password;
    }

    @Nullable
    @Override
    public String connect(URL url) {
        return doAuthentication(url);
    }

    @Nullable
    private String doAuthentication(URL jenkinsUrl) throws AuthenticationException {
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            final var targetHost = new HttpHost(jenkinsUrl.getHost(), jenkinsUrl.getPort(), jenkinsUrl.getProtocol());
            final var credentials = new UsernamePasswordCredentials(username, password);
            addAuthenticationPreemptive(targetHost, credentials);

            final var redirectTarget = getLastRedirectionHost(targetHost);
            addAuthenticationPreemptive(redirectTarget, credentials);
        }

        final var post = createPost(jenkinsUrl.toString(), Collections.emptyList());
        try {
            final var response = executeHttp(post);
            final var responseCode = response.getStatusLine().getStatusCode();
            final var responseBody = EntityUtils.toString(response.getEntity());
            LOG.trace(String.format("Call url '%s' --> Status: %s, Data %s", jenkinsUrl, responseCode,
                    responseBody));
            if (responseCode != HttpStatus.SC_OK) {
                checkResponse(responseCode, responseBody);
            }
            return responseBody;
        } catch (IOException ioEx) {
            throw new ConfigurationException(String.format("Error during authentication: %s", ioEx.getMessage()), ioEx);
        } finally {
            post.releaseConnection();
        }
    }
}
