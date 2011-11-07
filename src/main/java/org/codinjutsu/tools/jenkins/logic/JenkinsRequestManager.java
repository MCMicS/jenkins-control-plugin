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

package org.codinjutsu.tools.jenkins.logic;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.codinjutsu.tools.jenkins.security.SecurityClientFactory;
import org.codinjutsu.tools.jenkins.security.SecurityMode;
import org.codinjutsu.tools.jenkins.util.RssUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class JenkinsRequestManager {

    public static final int SUCCESS_ID = 0;
    public static final int UNAUTHORIZED_ID = 1;
    public static final int FAILED_ID = 2;

    private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;

    private static final String JENKINS_DESCRIPTION = "description";

    private static final String JOB = "job";
    private static final String JOB_NAME = "name";
    private static final String JOB_HEALTH = "healthReport";
    private static final String JOB_HEALTH_ICON = "iconUrl";
    private static final String JOB_URL = "url";
    private static final String JOB_COLOR = "color";
    private static final String JOB_LAST_BUILD = "lastBuild";
    private static final String JOB_IS_IN_QUEUE = "inQueue";

    private static final String VIEW = "view";
    private static final String PRIMARY_VIEW = "primaryView";
    private static final String VIEW_NAME = "name";

    private static final String VIEW_URL = "url";
    private static final String BUILD_IS_BUILDING = "building";
    private static final String BUILD_RESULT = "result";
    private static final String BUILD_URL = "url";
    private static final String BUILD_NUMBER = "number";

    private static final String RSS_ENTRY = "entry";
    private static final String RSS_TITLE = "title";
    private static final String RSS_LINK = "link";
    private static final String RSS_LINK_HREF = "href";

    private static final String TEST_CONNECTION_REQUEST = "/api/xml?tree=nodeName";

    private final UrlBuilder urlBuilder;

    private SecurityClient securityClient;


    public JenkinsRequestManager() {
        this(new UrlBuilder(), SecurityClientFactory.none());
    }


    public JenkinsRequestManager(UrlBuilder urlBuilder, SecurityClient securityClient) {
        this.urlBuilder = urlBuilder;
        this.securityClient = securityClient;
    }


    public Jenkins loadJenkinsWorkspace(JenkinsConfiguration configuration) throws Exception {
        URL url = urlBuilder.createJenkinsWorkspaceUrl(configuration);
        InputStream inputStream = null;
        try {
            inputStream = securityClient.executeAndGetResponseStream(url);
            Document doc = getXMLBuilder().build(inputStream);

            Jenkins jenkins = createJenkins(doc);
            jenkins.setPrimaryView(createPreferredView(doc));
            jenkins.setViews(createJenkinsViews(doc));

            return jenkins;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }


    public Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsConfiguration configuration) throws IOException, JDOMException {
        URL url = urlBuilder.createRssLatestUrl(configuration.getServerUrl());

        InputStream inputStream = null;
        try {
            inputStream = createInputStream(url);
            Document doc = getXMLBuilder().build(inputStream);

            return createLatestBuildList(doc);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }


    public List<Job> loadJenkinsView(String viewUrl) throws Exception {
        URL url = urlBuilder.createViewUrl(viewUrl);

        InputStream inputStream = null;
        try {
            inputStream = securityClient.executeAndGetResponseStream(url);
            Document doc = getXMLBuilder().build(inputStream);

            return createJenkinsJobs(doc);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }


    public Job loadJob(String jobUrl) throws Exception {
        URL url = urlBuilder.createJobUrl(jobUrl);
        InputStream inputStream = null;
        try {
            inputStream = securityClient.executeAndGetResponseStream(url);
            Document doc = getXMLBuilder().build(inputStream);

            Element jobElement = doc.getRootElement();

            String jobName = jobElement.getChildText(JOB_NAME);
            String jobColor = jobElement.getChildText(JOB_COLOR);
            String inQueue = jobElement.getChildText(JOB_IS_IN_QUEUE);
            String jobHealth = getJobHealth(jobElement);

            Job job = Job.createJob(jobName, jobColor, jobHealth, jobUrl, inQueue);
            Element lastBuild = jobElement.getChild(JOB_LAST_BUILD);
            if (lastBuild != null) {
                job.setLastBuild(createLastBuild(lastBuild));
            }

            return job;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }


    public void runBuild(Job job, JenkinsConfiguration configuration) throws Exception {
        URL url = urlBuilder.createRunJobUrl(job.getUrl(), configuration);
        securityClient.execute(url);
    }


    public AuthenticationResult authenticate(final String serverUrl, SecurityMode securityMode, final String username, final String password) {
        try {
            securityClient = SecurityClientFactory.create(securityMode, username, password);

//            securityClient.connect(new URL(serverUrl + TEST_CONNECTION_REQUEST));
            securityClient.connect(new URL(serverUrl));
            return AuthenticationResult.SUCCESSFULL;

        } catch (Exception e) {
            return AuthenticationResult.FAILED;
        }

    }


    private Jenkins createJenkins(Document doc) {
        Element jenkinsElement = doc.getRootElement();
        String description = jenkinsElement.getChildText(JENKINS_DESCRIPTION);
        if (description == null) {
            description = "";
        }
        return new Jenkins(description);
    }


    private Build createLastBuild(Element jobLastBuild) {
        String isBuilding = jobLastBuild.getChildText(BUILD_IS_BUILDING);
        String isSuccess = jobLastBuild.getChildText(BUILD_RESULT);
        String number = jobLastBuild.getChildText(BUILD_NUMBER);
        String buildUrl = jobLastBuild.getChildText(BUILD_URL);
        return Build.createBuild(buildUrl, number, isSuccess, isBuilding);
    }


    private View createPreferredView(Document doc) {

        Element primaryView = doc.getRootElement().getChild(PRIMARY_VIEW);
        if (primaryView != null) {
            String viewName = primaryView.getChildText(VIEW_NAME);
            String viewUrl = primaryView.getChildText(VIEW_URL);
            return View.createView(viewName, viewUrl);
        }
        return null;
    }


    private List<View> createJenkinsViews(Document doc) {
        List<View> views = new ArrayList<View>();

        List<Element> viewElement = doc.getRootElement().getChildren(VIEW);
        for (Element element : viewElement) {
            String viewName = element.getChildText(VIEW_NAME);
            String viewUrl = element.getChildText(VIEW_URL);
            views.add(View.createView(viewName, viewUrl));
        }

        return views;
    }


    private List<Job> createJenkinsJobs(Document doc) {
        List<Element> jobElements = doc.getRootElement().getChildren(JOB);
        List<Job> jobs = new LinkedList<Job>();
        for (Element jobElement : jobElements) {
            String jobName = jobElement.getChildText(JOB_NAME);
            String jobColor = jobElement.getChildText(JOB_COLOR);
            String jobUrl = jobElement.getChildText(JOB_URL);
            String inQueue = jobElement.getChildText(JOB_IS_IN_QUEUE);

            String jobHealth = getJobHealth(jobElement);


            Job job = Job.createJob(jobName, jobColor, jobHealth, jobUrl, inQueue);
            Element lastBuild = jobElement.getChild(JOB_LAST_BUILD);
            if (lastBuild != null) {
                job.setLastBuild(createLastBuild(lastBuild));
            }
            jobs.add(job);
        }
        return jobs;
    }

    private String getJobHealth(Element jobElement) {
        String jobHealth = null;
        Element jobHealthElement = jobElement.getChild(JOB_HEALTH);
        if (jobHealthElement != null) {
            jobHealth = jobHealthElement.getChildText(JOB_HEALTH_ICON);
            if (!StringUtils.isEmpty(jobHealth)) {
                if (jobHealth.endsWith(".png"))
                    jobHealth = jobHealth.substring(0, jobHealth.lastIndexOf(".png"));
                else {
                    jobHealth = jobHealth.substring(0, jobHealth.lastIndexOf(".gif"));
                }
            } else {
                jobHealth = null;
            }
        }
        return jobHealth;
    }


    private Map<String, Build> createLatestBuildList(Document doc) {

        Map<String, Build> buildMap = new HashMap<String, Build>();
        Element rootElement = doc.getRootElement();
        List<Element> elements = rootElement.getChildren(RSS_ENTRY, rootElement.getNamespace());
        for (Element element : elements) {
            String title = element.getChildText(RSS_TITLE, rootElement.getNamespace());
            String jobName = RssUtil.extractBuildJob(title);
            String number = RssUtil.extractBuildNumber(title);
            BuildStatusEnum status = RssUtil.extractStatus(title);
            Element linkElement = element.getChild(RSS_LINK, rootElement.getNamespace());
            String link = linkElement.getAttributeValue(RSS_LINK_HREF);

            Build currentBuild = buildMap.get(jobName);
            if (!BuildStatusEnum.NULL.equals(status)) {
                Build newBuild = Build.createBuild(link, number, status.getStatus(), Boolean.FALSE.toString());
                if (currentBuild == null || newBuild.isAfter(currentBuild)) {
                    buildMap.put(jobName, newBuild);
                }
            }
        }

        return buildMap;
    }


    private static InputStream createInputStream(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection(Proxy.NO_PROXY);
        urlConnection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
        urlConnection.setReadTimeout(DEFAULT_READ_TIMEOUT);
        return urlConnection.getInputStream();
    }

    private static SAXBuilder getXMLBuilder() {
        SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setValidation(false);
        return saxBuilder;
    }


//    private AuthenticationResult runCLICommand(CLICommandHandler cmdHandler) {
//        CLI cli = null;
//        try {
//            cli = new CLI(new URL(cmdHandler.getServerURL()));
//            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
//            InputStream stdIn = System.in;
//            PrintStream stdOut = System.out;
//            int returnCode = cli.execute(cmdHandler.getArgs(), stdIn, stdOut, stdErr);
//
//            cmdHandler.processStdStreams(stdIn, stdErr, stdErr);
//
//            if (returnCode == 0) {
//                return AuthenticationResult.SUCCESSFULL;
//            } else {
//                return AuthenticationResult.BAD_CREDENTIAL;
//            }
//        } catch (Exception ex) {
//            return AuthenticationResult.FAILED;
//        } finally {
//            if (cli != null) {
//                try {
//                    cli.close();
//                } catch (Exception interrEx) {
//                    throw new RuntimeException(interrEx.getMessage());
//                }
//            }
//        }
//    }


//    private interface CLICommandHandler {
//
//        String getServerURL();
//
//        List<String> getArgs();
//
//        void processStdStreams(InputStream stdIn, OutputStream stdOut, OutputStream stdErr);
//    }

}
