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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

abstract class AbstractSecurityClient implements SecurityClient {

    private static final String BAD_CRUMB_DATA = "No valid crumb was included in the request";
    static final String CRUMB_NAME = ".crumb";
    static final String TEST_CONNECTION_REQUEST = "/api/xml?tree=nodeName";

    private final String crumbDataFile;
    String crumbValue;

    final HttpClient httpClient;

    AbstractSecurityClient(HttpClient httpClient, String crumbDataFile) {
        this.httpClient = httpClient;
        this.crumbDataFile = crumbDataFile;
    }

    void setCrumbValueIfNeeded() throws IOException {
        if (!isCrumbDataSet()) {
            if (StringUtils.isNotEmpty(crumbDataFile)) {
                crumbValue = extractValueFromFile(crumbDataFile);
            }
        }
    }

    String extractValueFromFile(String file) throws IOException {
        String value = IOUtils.toString(new FileInputStream(file));
        if (StringUtils.isNotEmpty(value)) {
            value = StringUtils.removeEnd(value, "\n");
        }

        return value;
    }

    void checkResponse(int statusCode, String responseBody) throws AuthenticationException {
        if (statusCode == HttpURLConnection.HTTP_FORBIDDEN || statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (StringUtils.contains(responseBody, BAD_CRUMB_DATA)) {
                throw new AuthenticationException("CSRF enabled -> Missing or bad crumb data");
            }
            if (StringUtils.contains(responseBody, "Unauthorized")) {
                throw new AuthenticationException("Unauthorized -> Missing or bad credentials");
            }
            if (StringUtils.contains(responseBody, "Authentication required")) {
                throw new AuthenticationException("Authentication required");
            }
        }

        if (HttpURLConnection.HTTP_INTERNAL_ERROR == statusCode) {
            throw new AuthenticationException("Server Internal Error: Server unavailable");
        }
    }

    boolean isCrumbDataSet() {
        return crumbValue != null;
    }

    boolean isRedirection(int statusCode) {
        return statusCode / 100 == 3;
    }
}
