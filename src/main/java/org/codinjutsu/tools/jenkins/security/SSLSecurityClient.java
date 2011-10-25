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
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class SSLSecurityClient implements SecurityClient {


    private HttpClient client;

    private final String username;

    private final String password;
    private final ProtocolSocketFactory protocolSocketFactory;


    SSLSecurityClient() {
        this(null);
    }

    SSLSecurityClient(String username, String password) {
        this(null, username, password);
    }


    SSLSecurityClient(ProtocolSocketFactory protocolSocketFactory) {
        this(protocolSocketFactory, null, null);
    }

    SSLSecurityClient(ProtocolSocketFactory protocolSocketFactory, String username, String password) {
        this.protocolSocketFactory = protocolSocketFactory;
        this.username = username;
        this.password = password;
    }


    public void connect(URL jenkinsUrl) throws Exception {
        client = new HttpClient();

        if (protocolSocketFactory != null) {
            Protocol.registerProtocol("https", new Protocol("https", protocolSocketFactory, jenkinsUrl.getPort()));
        }

        if (areCredentialsSet()) {
            client.getState().setCredentials(
                    new AuthScope(jenkinsUrl.getHost(), jenkinsUrl.getPort()),
                    new UsernamePasswordCredentials(username, password));
            client.getParams().setAuthenticationPreemptive(true);
        }


        PostMethod postMethod = new PostMethod(jenkinsUrl.toString());

        if (areCredentialsSet()) {
            postMethod.setDoAuthentication(true);
        }
        int responseCode = client.executeMethod(postMethod);
        if (responseCode != HttpURLConnection.HTTP_OK) {
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new AuthenticationException("Bad Credentials.");
            }
        }

        postMethod.releaseConnection();
    }


    private boolean areCredentialsSet() {
        return username != null && password != null;
    }


    public String execute(URL url) throws Exception {
        PostMethod post = new PostMethod(url.toString());
        try {
            client.executeMethod(post);
            return post.getResponseBodyAsString();
        } finally {
            post.releaseConnection();
        }
    }

    public InputStream executeAndGetResponseStream(URL url) throws Exception {
        PostMethod post = new PostMethod(url.toString());
        try {
            client.executeMethod(post);
            return post.getResponseBodyAsStream();
        } finally {
            post.releaseConnection();
        }
    }


    public void close() throws Exception {
        client.getHttpConnectionManager().closeIdleConnections(1000);
        Protocol.unregisterProtocol("https");
    }
}
