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
import org.apache.commons.httpclient.util.URIUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class NoSecurityClient implements SecurityClient {
    private final HttpClient client;
    private String crumbName;
    private String crumbValue;


    NoSecurityClient() {
        this.client = new HttpClient();
    }


    public void connect(URL jenkinsUrl) throws Exception {
        getCrumbData(jenkinsUrl.toString());
    }


    private void getCrumbData(String baseUrlStr) throws Exception {
        URL breadCrumbUrl = new URL(URIUtil.encodePathQuery(baseUrlStr + "/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)"));
        HttpURLConnection urlConnection = (HttpURLConnection) breadCrumbUrl.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String crumbData = reader.readLine();
        String[] crumbNameValue = crumbData.split(":");
        crumbName = crumbNameValue[0];
        crumbValue = crumbNameValue[1];

        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new AuthenticationException("This Jenkins server requires authentication!");
        }
    }


    public String execute(URL url) throws Exception {
        String urlStr = url.toString();
        PostMethod post = new PostMethod(urlStr);
        if (!isCrumbDataSet()) {
            getCrumbData(urlStr);
        }
        post.addRequestHeader(crumbName, crumbValue);
        try {
            client.executeMethod(post);
            return post.getResponseBodyAsString();
        } finally {
            post.releaseConnection();
        }
    }


    public InputStream executeAndGetResponseStream(URL url) throws Exception {
        String urlStr = url.toString();
        PostMethod post = new PostMethod(urlStr);
        if (!isCrumbDataSet()) {
            getCrumbData(urlStr);
        }
        post.addRequestHeader(crumbName, crumbValue);
//        try {
        client.executeMethod(post);
        return post.getResponseBodyAsStream();
//        } finally {
//            post.releaseConnection();
//        }
    }


    public boolean isCrumbDataSet() {
        return crumbName != null && crumbValue != null;
    }
}
