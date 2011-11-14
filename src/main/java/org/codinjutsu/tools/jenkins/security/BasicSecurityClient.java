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
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


class BasicSecurityClient implements SecurityClient {

    private URL master;

    private final String username;
    private final String passwordFile;
    private String password = null;

    private final HttpClient client;
    private String crumbName;
    private String crumbValue;

    private static final String TEST_CONNECTION_REQUEST = "/api/xml?tree=nodeName";
    private PostMethod currentPostMethod;

    BasicSecurityClient(String username, String passwordFile) {
        this.client = new HttpClient();
        this.username = username;
        this.passwordFile = passwordFile;
    }


    public void connect(URL jenkinsUrl) throws Exception {
        master = new URL(jenkinsUrl.toString() + TEST_CONNECTION_REQUEST);

        if (!isCrumbDataSet()) {
            getCrumbData(jenkinsUrl.toString());
        }


        if (passwordFile != null) {
            password = IOUtils.toString(new FileInputStream(passwordFile));
            if (StringUtils.isNotEmpty(password)) {
                password = StringUtils.removeEnd(password, "\n");
            }
        }

        if (password == null && username == null) {
            checkJenkinsSecurity();
        }
        doAuthentication();


    }


    private boolean isCrumbDataSet() {
        return crumbName != null && crumbValue != null;
    }


    public String execute(URL url) throws Exception {
        String urlStr = url.toString();
        PostMethod postMethod = new PostMethod(urlStr);
        if (!isCrumbDataSet()) {
            getCrumbData(urlStr);
        }
        try {
            client.executeMethod(postMethod);
            checkStatusCode(postMethod.getStatusCode());
            return postMethod.getResponseBodyAsString();
        } finally {
            postMethod.releaseConnection();
        }
    }


    private static void checkStatusCode(int statusCode) throws AuthenticationException {
        if (HttpURLConnection.HTTP_FORBIDDEN == statusCode) {
            throw new AuthenticationException("Forbidden");
        }
        if (HttpURLConnection.HTTP_INTERNAL_ERROR == statusCode) {
            throw new AuthenticationException("Server Internal Error");
        }
    }


    public InputStream executeAndGetResponseStream(URL url) throws Exception {
        String urlStr = url.toString();
        currentPostMethod = new PostMethod(urlStr);
        if (!isCrumbDataSet()) {
            getCrumbData(urlStr);
        }

        client.executeMethod(currentPostMethod);
        checkStatusCode(currentPostMethod.getStatusCode());
        return currentPostMethod.getResponseBodyAsStream();
    }

    public void releasePostConnection() {
        currentPostMethod.releaseConnection();
    }


    private void getCrumbData(String baseUrlStr) throws IOException {
        URL breadCrumbUrl = new URL(URIUtil.encodePathQuery(baseUrlStr + "/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)"));
        URLConnection urlConnection = breadCrumbUrl.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String crumbData = reader.readLine();
        String[] crumbNameValue = crumbData.split(":");
        crumbName = crumbNameValue[0];
        crumbValue = crumbNameValue[1];
    }


    private void doAuthentication() throws IOException, AuthenticationException {

        if (username != null && password != null) {
            client.getState().setCredentials(
                    new AuthScope(master.getHost(), master.getPort()),
                    new UsernamePasswordCredentials(username, password));
        }


        client.getParams().setAuthenticationPreemptive(true);
        client.getParams().setParameter("http.protocol.handle-redirects", false);

        PostMethod postMethod = new PostMethod(master.toString());


        postMethod.setDoAuthentication(true);
        postMethod.setFollowRedirects(false);
        int responseCode = client.executeMethod(postMethod);
        if (responseCode != HttpURLConnection.HTTP_OK) {
            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED || responseCode == HttpURLConnection.HTTP_FORBIDDEN
                    || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                throw new AuthenticationException("Bad Credentials.");
            }
        }

        postMethod.releaseConnection();
    }


    private void checkJenkinsSecurity() throws AuthenticationException {
        try {
            HttpURLConnection con = (HttpURLConnection) master
                    .openConnection();
            con.connect();

            if (con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new AuthenticationException("This Jenkins server requires authentication!");
            }

            String jenkinsHeader = con.getHeaderField("X-Jenkins");
            if (jenkinsHeader == null) {
                throw new AuthenticationException("This URL doesn't look like Jenkins.");
            }
        } catch (IOException ioEx) {
            throw new AuthenticationException("Failed to connect to " + master, ioEx);
        }
    }


}
