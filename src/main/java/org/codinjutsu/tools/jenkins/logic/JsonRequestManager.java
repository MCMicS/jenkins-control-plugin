/*
 * Copyright (c) 2012 David Boissier
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
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
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonRequestManager implements RequestManager {

    private static final Logger LOG = Logger.getLogger(JsonRequestManager.class);
    public static final String BUILDHIVE_CLOUDBEES = "buildhive";

    private UrlBuilder urlBuilder;
    private SecurityClient securityClient;

    private JenkinsPlateform jenkinsPlateform = JenkinsPlateform.CLASSIC;

    private final JsonFactory factory;


    public JsonRequestManager(SecurityClient securityClient) {
        this.urlBuilder = UrlBuilder.json();
        this.securityClient = securityClient;
        factory = new JsonFactory();
    }

    public JsonRequestManager(String crumbFile) {
        this(SecurityClientFactory.none(crumbFile));
    }

    public Jenkins loadJenkinsWorkspace(JenkinsConfiguration configuration) {
        URL url = urlBuilder.createJenkinsWorkspaceUrl(configuration);
        String jenkinsWorkspaceData = securityClient.execute(url);

        if (configuration.getServerUrl().contains(BUILDHIVE_CLOUDBEES)) {//TODO hack need to refactor
            jenkinsPlateform = JenkinsPlateform.CLOUDBEES;
        } else {
            jenkinsPlateform = JenkinsPlateform.CLASSIC;
        }

        Jenkins jenkins = createWorkspace(jenkinsWorkspaceData, configuration.getServerUrl());

        int jenkinsPort = url.getPort();
        URL viewUrl = urlBuilder.createViewUrl(jenkinsPlateform, jenkins.getPrimaryView().getUrl());
        int viewPort = viewUrl.getPort();

        if (isJenkinsPortSet(jenkinsPort) && jenkinsPort != viewPort) {
            throw new ConfigurationException(String.format("Jenkins Port seems to be incorrect in the Server configuration page. Please fix 'Jenkins URL' at %s/configure", configuration.getServerUrl()));
        }

        return jenkins;
    }

    private boolean isJenkinsPortSet(int jenkinsPort) {
        return jenkinsPort != -1;
    }

    public Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsConfiguration configuration) {
        URL url = urlBuilder.createRssLatestUrl(configuration.getServerUrl());

        String rssData = securityClient.execute(url);
        Document doc = buildDocument(rssData);

        return createLatestBuildList(doc);
    }

    public List<Job> loadJenkinsView(String viewUrl) {
        URL url = urlBuilder.createViewUrl(jenkinsPlateform, viewUrl);
        String jenkinsViewData = securityClient.execute(url);
        if (jenkinsPlateform.equals(JenkinsPlateform.CLASSIC)) {
            return createViewJobs(jenkinsViewData);
        } else {
            return createCloudbeesViewJobs(jenkinsViewData);
        }
    }

    public Job loadJob(String jenkinsJobUrl) {
        URL url = urlBuilder.createJobUrl(jenkinsJobUrl);
        String jenkinsJobData = securityClient.execute(url);
        return createJob(jenkinsJobData);
    }


    public void runBuild(Job job, JenkinsConfiguration configuration) {
        URL url = urlBuilder.createRunJobUrl(job.getUrl(), configuration);
        securityClient.execute(url);
    }

    public void runParameterizedBuild(Job job, JenkinsConfiguration configuration, Map<String, String> paramValueMap) {
        URL url = urlBuilder.createRunParameterizedJobUrl(job.getUrl(), configuration, paramValueMap);
        securityClient.execute(url);
    }

    public void authenticate(String serverUrl, SecurityMode securityMode, String username, String passwordFile, String crumbDataFile) {
        securityClient = SecurityClientFactory.create(securityMode, username, passwordFile, crumbDataFile);
        securityClient.connect(urlBuilder.createAuthenticationUrl(serverUrl));
    }

    public List<Job> loadFavoriteJobs(List<JenkinsConfiguration.FavoriteJob> favoriteJobs) {
        throw new UnsupportedOperationException();
    }

    public void setJenkinsPlateform(JenkinsPlateform jenkinsPlateform) {
        this.jenkinsPlateform = jenkinsPlateform;
    }


    public Job createJob(String jsonData) {
        try {
            JsonParser jsonParser = factory.createJsonParser(jsonData);
            Job job = createJob(jsonParser);
            jsonParser.close();
            return job;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Job createJob(JsonParser jsonParser) throws IOException {
        Job job = new Job();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String currentName = jsonParser.getCurrentName();
            escapeFieldName(jsonParser);

            if ("name".equals(currentName)) {
                job.setName(jsonParser.getText());
            } else if ("url".equals(currentName)) {
                job.setUrl(jsonParser.getText());
            } else if ("color".equals(currentName)) {
                job.setColor(jsonParser.getText());
            } else if ("healthReport".equals(currentName)) {
                job.setHealth(getHealth(jsonParser));
            } else if ("buildable".equals(currentName)) {
                job.setBuildable(jsonParser.getBooleanValue());
            } else if ("inQueue".equals(currentName)) {
                job.setInQueue(jsonParser.getBooleanValue());
            } else if ("lastBuild".equals(currentName)) {
                job.setLastBuild(getLastBuild(jsonParser));
            } else if ("property".equals(currentName)) {
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                        String currentNameInProperty = jsonParser.getCurrentName();
                        if ("parameterDefinitions".equals(currentNameInProperty)) {
                            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                JobParameter jobParameter = new JobParameter();
                                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                    escapeFieldName(jsonParser);
                                    String currentNameInParameterDefinition = jsonParser.getCurrentName();
                                    if ("defaultParameterValue".equals(currentNameInParameterDefinition)) {
                                        if (JsonToken.VALUE_NULL.equals(jsonParser.getCurrentToken())) {
                                            continue;
                                        }
                                        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                                            escapeFieldName(jsonParser);
                                            String currentNameInDefaultParameterValue = jsonParser.getCurrentName();
                                            if ("value".equals(currentNameInDefaultParameterValue)) {
                                                jobParameter.setDefaultValue(jsonParser.getText());
                                            }
                                        }
                                    } else if ("name".equals(currentNameInParameterDefinition)) {
                                        jobParameter.setName(jsonParser.getText());
                                    } else if ("type".equals(currentNameInParameterDefinition)) {
                                        jobParameter.setType(jsonParser.getText());
                                    } else if ("choice".equals(currentNameInParameterDefinition)) {
                                        jobParameter.setChoices(createChoices(jsonParser));
                                    }
                                }
                                job.addParameter(jobParameter);
                            }
                        }
                    }
                }
            }
        }

        return job;
    }

    private List<String> createChoices(JsonParser jsonParser) throws IOException {
        List<String> choices = new LinkedList<String>();
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            escapeFieldName(jsonParser);
            String text = jsonParser.getText();
            choices.add(text);
        }
        return choices;
    }

    private void escapeFieldName(JsonParser jsonParser) throws IOException {
        if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME) {
            jsonParser.nextToken();
        }
    }

    private Job.Health getHealth(JsonParser jsonParser) throws IOException {
        Job.Health health = new Job.Health();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            escapeFieldName(jsonParser);

            String currentNameInHealthReport = jsonParser.getCurrentName();
            if ("description".equals(currentNameInHealthReport)) {
                health.setDescription(jsonParser.getText());
            } else if ("iconUrl".equals(currentNameInHealthReport)) {
                String jobHealthLevel = jsonParser.getText();
                if (StringUtils.isNotEmpty(jobHealthLevel)) {
                    if (jobHealthLevel.endsWith(".png"))
                        jobHealthLevel = jobHealthLevel.substring(0, jobHealthLevel.lastIndexOf(".png"));
                    else {
                        jobHealthLevel = jobHealthLevel.substring(0, jobHealthLevel.lastIndexOf(".gif"));
                    }
                } else {
                    jobHealthLevel = null;
                }
                health.setLevel(jobHealthLevel);
            }
        }
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            jsonParser.nextToken();
        }
        if (!StringUtils.isEmpty(health.getLevel())) {
            return health;
        } else {
            return null;
        }
    }

    private Build getLastBuild(JsonParser jsonParser) throws IOException {
        Build lastBuild = new Build();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            escapeFieldName(jsonParser);

            String currentNameInLastBuild = jsonParser.getCurrentName();
            if ("id".equals(currentNameInLastBuild)) {
                lastBuild.setBuildDate(jsonParser.getText());
            } else if ("building".equals(currentNameInLastBuild)) {
                lastBuild.setBuilding(jsonParser.getBooleanValue());
            } else if ("number".equals(currentNameInLastBuild)) {
                lastBuild.setNumber(jsonParser.getIntValue());
            } else if ("result".equals(currentNameInLastBuild)) {
                lastBuild.setStatus(jsonParser.getText());
            } else if ("url".equals(currentNameInLastBuild)) {
                lastBuild.setUrl(jsonParser.getText());
            }
        }
        return lastBuild;
    }

    public Jenkins createWorkspace(String jsonData, String serverUrl) {
        Jenkins jenkins = new Jenkins("", serverUrl);
        try {
            JsonParser jsonParser = factory.createJsonParser(jsonData);
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String currentName = jsonParser.getCurrentName();
                escapeFieldName(jsonParser);

                if ("primaryView".equals(currentName)) {
                    jenkins.setPrimaryView(getView(jsonParser));

                } else if ("views".equals(currentName)) {
                    jenkins.setViews(getViews(jsonParser));
                }
            }
            jsonParser.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jenkins;
    }

    private List<View> getViews(JsonParser jsonParser) throws IOException {
        List<View> views = new LinkedList<View>();
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            View view = getView(jsonParser);
            views.add(view);
        }
        return views;
    }

    private View getView(JsonParser jsonParser) throws IOException {
        View view = new View();
        view.setNested(false);
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            escapeFieldName(jsonParser);

            String currentName = jsonParser.getCurrentName();
            if ("name".equals(currentName)) {
                view.setName(jsonParser.getText());
            } else if ("url".equals(currentName)) {
                view.setUrl(jsonParser.getText());
            } else if ("views".equals(currentName)) {
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    View nestedView = getNestedView(jsonParser);
                    view.addSubView(nestedView);
                }
            }
        }
        return view;
    }

    private View getNestedView(JsonParser jsonParser) throws IOException {
        View nestedView = new View();
        nestedView.setNested(true);
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            escapeFieldName(jsonParser);

            String currentName = jsonParser.getCurrentName();
            if ("name".equals(currentName)) {
                nestedView.setName(jsonParser.getText());
            } else if ("url".equals(currentName)) {
                nestedView.setUrl(jsonParser.getText());
            }
        }
        return nestedView;
    }

    public List<Job> createViewJobs(String jsonData) {
        try {
            JsonParser jsonParser = factory.createJsonParser(jsonData);
            List<Job> jobs = createJobs(jsonParser);
            jsonParser.close();
            return jobs;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Job> createJobs(JsonParser jsonParser) throws IOException {
        List<Job> jobs = new LinkedList<Job>();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            escapeFieldName(jsonParser);

            String currentName = jsonParser.getCurrentName();

            if ("jobs".equals(currentName)) {
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    jobs.add(createJob(jsonParser));
                }
            }
        }
        return jobs;
    }

    public List<Job> createCloudbeesViewJobs(String jsonData) {
        try {
            JsonParser jsonParser = factory.createJsonParser(jsonData);
            List<Job> jobs = new LinkedList<Job>();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                escapeFieldName(jsonParser);
                String currentName = jsonParser.getCurrentName();
                if ("views".equals(currentName)) {
                    while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                        String currentNameInView = jsonParser.getCurrentName();
                        if ("jobs".equals(currentNameInView)) {
                            List<Job> returnedJobs = createJobs(jsonParser);
                            jobs.addAll(returnedJobs);
                        }
                    }
                }
            }

            jsonParser.close();
            return jobs;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Document buildDocument(String jenkinsXmlData) {
        Reader jenkinsDataReader = new StringReader(jenkinsXmlData);
        try {
            return new SAXBuilder(false).build(jenkinsDataReader);
        } catch (JDOMException e) {
            LOG.error("Invalid data received from the Jenkins Server. Actual :\n" + jenkinsXmlData, e);
            throw new RuntimeException("Invalid data received from the Jenkins Server. Please retry");
        } catch (IOException e) {
            LOG.error("Error during analyzing the Jenkins data.", e);
            throw new RuntimeException("Error during analyzing the Jenkins data.");
        } finally {
            IOUtils.closeQuietly(jenkinsDataReader);
        }
    }

    private Map<String, Build> createLatestBuildList(Document doc) {

        Map<String, Build> buildMap = new LinkedHashMap<String, Build>();
        Element rootElement = doc.getRootElement();

        List<Element> elements = rootElement.getChildren(RSS_ENTRY, rootElement.getNamespace());
        for (Element element : elements) {
            String title = element.getChildText(RSS_TITLE, rootElement.getNamespace());
            String publishedBuild = element.getChildText(RSS_PUBLISHED, rootElement.getNamespace());
            String jobName = RssUtil.extractBuildJob(title);
            String number = RssUtil.extractBuildNumber(title);
            BuildStatusEnum status = RssUtil.extractStatus(title);
            Element linkElement = element.getChild(RSS_LINK, rootElement.getNamespace());
            String link = linkElement.getAttributeValue(RSS_LINK_HREF);

            if (!BuildStatusEnum.NULL.equals(status)) {
                buildMap.put(jobName, Build.createBuildFromRss(link, number, status.getStatus(), Boolean.FALSE.toString(), publishedBuild, title));

            }

        }

        return buildMap;
    }

}
