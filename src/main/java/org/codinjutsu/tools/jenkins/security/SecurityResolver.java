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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SecurityResolver {

    public static SecurityMode resolve(String serverUrl) throws AuthenticationException {

        URL url;
        try {
            url = new URL(serverUrl);
        } catch (MalformedURLException urlEx) {
            throw new AuthenticationException(urlEx.getMessage());
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                return SecurityMode.BASIC;
            }

            String jenkinsHeader = getServerHeader(connection);
            if (jenkinsHeader == null) {
                throw new AuthenticationException("This URL doesn't look like Jenkins/Hudson.");
            }
        } catch (IOException ioEx) {
            throw new AuthenticationException(ioEx.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return SecurityMode.NONE;
    }

    private static String getServerHeader(HttpURLConnection connection) {
        String jenkinsHeader = connection.getHeaderField("X-Jenkins");
        if (jenkinsHeader == null) {
            jenkinsHeader = connection.getHeaderField("X-Hudson");
        }
        return jenkinsHeader;
    }
}
