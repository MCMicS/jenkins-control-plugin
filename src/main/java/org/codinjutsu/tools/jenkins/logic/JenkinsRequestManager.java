package org.codinjutsu.tools.jenkins.logic;

import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface JenkinsRequestManager {

    Jenkins loadJenkinsWorkspace(JenkinsConfiguration configuration)
            throws IOException, JDOMException;


    Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsConfiguration configuration)
            throws JDOMException, IOException;


    List<Job> loadJenkinsView(String viewUrl) throws JDOMException, IOException;

    Job loadJob(String jobUrl) throws JDOMException, IOException;


    AuthenticationResult runBuild(Job job, JenkinsConfiguration configuration) throws IOException;

    AuthenticationResult testConnexion(String serverUrl, boolean enableAuthentication, String username, String password);
}
