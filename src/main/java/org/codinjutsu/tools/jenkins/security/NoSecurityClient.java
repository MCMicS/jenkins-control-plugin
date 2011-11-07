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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class NoSecurityClient implements SecurityClient {
    private final HttpClient client;


    NoSecurityClient() {
        this.client = new HttpClient();
    }


    public void connect(URL jenkinsUrl) throws Exception {
        try {
            HttpURLConnection con = (HttpURLConnection) jenkinsUrl.openConnection();
            con.connect();

            if (con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new AuthenticationException("This Jenkins server requires authentication!");
            }

            String v = con.getHeaderField("X-Jenkins");
            if (v == null) {
                throw new AuthenticationException("This URL doesn't look like Jenkins.");
            }
        } catch (IOException e) {
            throw new AuthenticationException("Failed to connect to " + jenkinsUrl, e);
        }
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


}
