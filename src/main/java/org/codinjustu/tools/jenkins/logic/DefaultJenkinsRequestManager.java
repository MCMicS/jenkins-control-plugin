package org.codinjustu.tools.jenkins.logic;

import org.codinjustu.tools.jenkins.JenkinsConfiguration;
import org.codinjustu.tools.jenkins.model.*;
import org.codinjustu.tools.jenkins.util.RssUtil;
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

@SuppressWarnings({"unchecked"})
public class DefaultJenkinsRequestManager implements JenkinsRequestManager {

    private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;

    private static final String JENKINS_DESCRIPTION = "description";

    private static final String JOB = "job";
    private static final String JOB_NAME = "name";
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

    private final UrlBuilder urlBuilder;


    public DefaultJenkinsRequestManager() {
        this(new UrlBuilder());
    }


    DefaultJenkinsRequestManager(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }


    public Jenkins loadJenkinsWorkspace(JenkinsConfiguration configuration)
            throws IOException, JDOMException {
        URL url = urlBuilder.createJenkinsWorkspaceUrl(configuration);

        InputStream inputStream = null;
        try {
            inputStream = createInputStream(url);
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


    public Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsConfiguration configuration)
            throws JDOMException, IOException {
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


    public List<Job> loadJenkinsView(String viewUrl) throws JDOMException, IOException {
        URL url = urlBuilder.createViewUrl(viewUrl);

        InputStream inputStream = null;
        try {
            inputStream = createInputStream(url);
            Document doc = getXMLBuilder().build(inputStream);

            return createJenkinsJobs(doc);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public Job loadJob(String jobUrl) throws JDOMException, IOException {
        URL url = urlBuilder.createJobUrl(jobUrl);
        InputStream inputStream = null;
        try {
            inputStream = createInputStream(url);
            Document doc = getXMLBuilder().build(inputStream);

            Element element = doc.getRootElement();

            String jobName = element.getChildText(JOB_NAME);
            String jobColor = element.getChildText(JOB_COLOR);
            String inQueue = element.getChildText(JOB_IS_IN_QUEUE);

            Job job = Job.createJob(jobName, jobColor, jobUrl, inQueue);
            Element lastBuild = element.getChild(JOB_LAST_BUILD);
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


    public void runBuild(Job job, JenkinsConfiguration configuration) throws IOException {
        URL url = urlBuilder.createRunJobUrl(job.getUrl(), configuration);

        InputStream inputStream = null;
        try {
            inputStream = createInputStream(url);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
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
        return null;//TODO créer par défaut la vue all/Tous
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


    private List<Job> createJenkinsJobs(Document doc) throws JDOMException, IOException {
        List<Element> jobElements = doc.getRootElement().getChildren(JOB);
        List<Job> jobs = new LinkedList<Job>();
        for (Element element : jobElements) {
            String jobName = element.getChildText(JOB_NAME);
            String jobColor = element.getChildText(JOB_COLOR);
            String jobUrl = element.getChildText(JOB_URL);
            String inQueue = element.getChildText(JOB_IS_IN_QUEUE);

            Job job = Job.createJob(jobName, jobColor, jobUrl, inQueue);
            Element lastBuild = element.getChild(JOB_LAST_BUILD);
            if (lastBuild != null) {
                job.setLastBuild(createLastBuild(lastBuild));
            }
            jobs.add(job);
        }
        return jobs;
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
}
