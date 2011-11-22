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

    private static final String CRUMB_REQUEST = "/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)";


    public static SecurityMode resolve(String serverUrl) throws AuthenticationException {

        URL url;
        try {
            url = new URL(serverUrl + "/api/xml?tree=nodeName");
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


    //
//    private void getCrumbData() throws Exception {
//        URL breadCrumbUrl = new URL(URIUtil.encodePathQuery(serverUrl + CRUMB_REQUEST));
//        HttpURLConnection urlConnection;
//        try {
//            urlConnection = (HttpURLConnection) breadCrumbUrl.openConnection();
//
//            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
//                return;
//            }
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//            String crumbData = reader.readLine();
//            if (StringUtils.isNotEmpty(crumbData) && crumbData.contains("crumb")) {
//                String[] crumbNameValue = crumbData.split(":");
//                crumbName = crumbNameValue[0];
//                crumbValue = crumbNameValue[1];
//            }
//        } catch (IOException ioEx) {
//            if (ioEx.getMessage().contains(Integer.toString(HttpURLConnection.HTTP_FORBIDDEN))) {
//                throw new AuthenticationException("If Cross Request Site Forgery Protection is set,\n Anonymous users should have at least read-only access");
//            }
//        }
}
