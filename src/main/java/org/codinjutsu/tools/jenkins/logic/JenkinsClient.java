package org.codinjutsu.tools.jenkins.logic;

import org.codinjutsu.tools.jenkins.security.SecurityClient;

import java.net.URI;

public class JenkinsClient extends com.offbytwo.jenkins.client.JenkinsHttpClient {

    public JenkinsClient(URI uri, SecurityClient securityClient) {
        super(uri, securityClient.getHttpClient());
        setLocalContext(securityClient.getHttpClientContext());
    }
}
