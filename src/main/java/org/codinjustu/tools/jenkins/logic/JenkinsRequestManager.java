package org.codinjustu.tools.jenkins.logic;

import org.codinjustu.tools.jenkins.JenkinsConfiguration;
import org.codinjustu.tools.jenkins.model.Build;
import org.codinjustu.tools.jenkins.model.Jenkins;
import org.codinjustu.tools.jenkins.model.Job;
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


    void runBuild(Job job, JenkinsConfiguration configuration) throws IOException;
}
