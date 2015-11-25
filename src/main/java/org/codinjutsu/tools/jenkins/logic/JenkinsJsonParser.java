/*
 * Copyright (c) 2013 David Boissier
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
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.LinkedList;
import java.util.List;

public class JenkinsJsonParser implements JenkinsParser {

    private static final Logger LOG = Logger.getLogger(JenkinsJsonParser.class);

    private static boolean getBoolean(Object obj) {
        return Boolean.TRUE.equals(obj);
    }

    @Override
    public Jenkins createWorkspace(String jsonData, String serverUrl) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        JSONParser parser = new JSONParser();
        Jenkins jenkins = new Jenkins("", serverUrl);

        try {
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            JSONObject primaryViewObject = (JSONObject) jsonObject.get(PRIMARY_VIEW);
            if (primaryViewObject != null) {
                jenkins.setPrimaryView(getView(primaryViewObject));
            }

            JSONArray viewsObject = (JSONArray) jsonObject.get(VIEWS);
            if (viewsObject != null) {
                jenkins.setViews(getViews(viewsObject));
            }

        } catch (ParseException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
        return jenkins;
    }

    private List<View> getViews(JSONArray viewsObjects) {
        List<View> views = new LinkedList<View>();
        for (Object obj : viewsObjects) {
            JSONObject viewObject = (JSONObject) obj;
            views.add(getView(viewObject));
        }

        return views;
    }

    private View getView(JSONObject viewObject) {
        View view = new View();
        view.setNested(false);
        String name = (String) viewObject.get(VIEW_NAME);
        if (name != null) {
            view.setName(name.toString());
        }

        String url = (String) viewObject.get(VIEW_URL);
        if (name != null) {
            view.setUrl(url.toString());
        }

        JSONArray subViewObjs = (JSONArray) viewObject.get(VIEWS);
        if (subViewObjs != null) {
            for (Object obj : subViewObjs) {
                JSONObject subviewObj = (JSONObject) obj;

                View nestedView = new View();
                nestedView.setNested(true);

                String currentName = (String) subviewObj.get(VIEW_NAME);
                nestedView.setName(currentName);

                String subViewUrl = (String) subviewObj.get(VIEW_URL);
                nestedView.setUrl(subViewUrl);

                view.addSubView(nestedView);
            }
        }
        return view;
    }

    @Override
    public Job createJob(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);

            return getJob(jsonObject);

        } catch (ParseException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }

    }

    public Build createBuild(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);

            return getBuild(jsonObject);

        } catch (ParseException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }

    private Build getBuild(JSONObject lastBuildObject) {
        if (lastBuildObject == null) {
            return null;
        }

        Build build = new Build();
        String buildDate = (String) lastBuildObject.get(BUILD_ID);
        build.setBuildDate(buildDate);
        final boolean building = getBoolean(lastBuildObject.get(BUILD_IS_BUILDING));
        build.setBuilding(building);
        Long number = (Long) lastBuildObject.get(BUILD_NUMBER);
        build.setNumber(number.intValue());
        String status = (String) lastBuildObject.get(BUILD_RESULT);
        build.setStatus(status);
        String url = (String) lastBuildObject.get(BUILD_URL);
        build.setUrl(url);

        return build;
    }

    private Job getJob(JSONObject jsonObject) {
        Job job = new Job();
        String name = (String) jsonObject.get(JOB_NAME);
        job.setName(name);

        String displayName = (String) jsonObject.get(JOB_DISPLAY_NAME);
        job.setDisplayName(displayName);

        String url = (String) jsonObject.get(JOB_URL);
        job.setUrl(url);

        String color = (String) jsonObject.get(JOB_COLOR);
        job.setColor(color);
        JSONArray healths = (JSONArray) jsonObject.get(JOB_HEALTH);
        job.setHealth(getHealth(healths));
        final boolean buildable = getBoolean(jsonObject.get(JOB_IS_BUILDABLE));
        job.setBuildable(buildable);
        final boolean inQueue = getBoolean(jsonObject.get(JOB_IS_IN_QUEUE));
        job.setInQueue(inQueue);

        JSONObject lastBuildObject = (JSONObject) jsonObject.get(JOB_LAST_BUILD);
        job.setLastBuild(getLastBuild(lastBuildObject));
        JSONArray parameterProperty = (JSONArray) jsonObject.get(PARAMETER_PROPERTY);
        job.addParameters(getParameters(parameterProperty));
        return job;
    }

    private List<JobParameter> getParameters(JSONArray parameterProperties) {
        List<JobParameter> jobParameters = new LinkedList<JobParameter>();
        if (parameterProperties == null || parameterProperties.isEmpty()) {
            return jobParameters;
        }

        for (Object obj : parameterProperties) {
            JSONObject parameterProperty = (JSONObject) obj;
            if (parameterProperty == null || parameterProperty.isEmpty()) {
                continue;
            }


            JSONArray definitions = (JSONArray) parameterProperty.get(PARAMETER_DEFINITIONS);
            for (Object defObj : definitions) {
                JSONObject parameterObj = (JSONObject) defObj;
                JobParameter jobParameter = new JobParameter();
                JSONObject defaultParamObj = (JSONObject) parameterObj.get(PARAMETER_DEFAULT_PARAM);
                if (defaultParamObj != null && !defaultParamObj.isEmpty()) {
                    Object defaultValue = defaultParamObj.get(PARAMETER_DEFAULT_PARAM_VALUE);
                    if (defaultValue != null) {
                        jobParameter.setDefaultValue(defaultValue.toString());
                    }
                }

                String name = (String) parameterObj.get(PARAMETER_NAME);
                jobParameter.setName(name);
                String type = (String) parameterObj.get(PARAMETER_TYPE);
                jobParameter.setType(type);
                JSONArray choices = (JSONArray) parameterObj.get(PARAMETER_CHOICE);
                jobParameter.setChoices(getChoices(choices));

                jobParameters.add(jobParameter);
            }
        }
        return jobParameters;
    }

    private List<String> getChoices(JSONArray choiceObjs) {
        List<String> choices = new LinkedList<String>();
        if (choiceObjs == null || choiceObjs.isEmpty()) {
            return choices;
        }
        for (Object choiceObj : choiceObjs) {
            choices.add((String) choiceObj);
        }
        return choices;
    }

    private Build getLastBuild(JSONObject lastBuildObject) {
        return getBuild(lastBuildObject);
    }

    private Job.Health getHealth(JSONArray healths) {
        if (healths == null || healths.isEmpty()) {
            return null;
        }

        Job.Health health = new Job.Health();
        JSONObject healthObject = (JSONObject) healths.get(0);
        String description = (String) healthObject.get(JOB_HEALTH_DESCRIPTION);
        health.setDescription(description);
        String healthLevel = (String) healthObject.get(JOB_HEALTH_ICON);
        if (StringUtils.isNotEmpty(healthLevel)) {
            if (healthLevel.endsWith(".png"))
                healthLevel = healthLevel.substring(0, healthLevel.lastIndexOf(".png"));
            else {
                healthLevel = healthLevel.substring(0, healthLevel.lastIndexOf(".gif"));
            }
        } else {
            healthLevel = null;
        }

        health.setLevel(healthLevel);

        if (!StringUtils.isEmpty(health.getLevel())) {
            return health;
        } else {
            return null;
        }
    }

    @Override
    public List<Job> createViewJobs(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        JSONParser parser = new JSONParser();

        try {
            List<Job> jobs = new LinkedList<Job>();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            JSONArray jobObjects = (JSONArray) jsonObject.get(JOBS);
            for (Object object : jobObjects) {
                JSONObject jobObject = (JSONObject) object;
                jobs.add(getJob(jobObject));
            }

            return jobs;
        } catch (ParseException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Job> createCloudbeesViewJobs(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        JSONParser parser = new JSONParser();

        try {
            List<Job> jobs = new LinkedList<Job>();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            JSONArray viewObjs = (JSONArray) jsonObject.get(VIEWS);
            if (viewObjs == null && viewObjs.isEmpty()) {
                return jobs;
            }

            JSONObject viewJobObj = (JSONObject) viewObjs.get(0);
            if (viewJobObj == null) {
                return jobs;
            }

            JSONArray jobObjs = (JSONArray) viewJobObj.get(JOBS);
            for (Object obj : jobObjs) {
                JSONObject jobObj = (JSONObject) obj;
                jobs.add(getJob(jobObj));
            }

            return jobs;
        } catch (ParseException e) {
            String message = String.format("Error during parsing JSON data : %s", jsonData);
            LOG.error(message, e);
            throw new RuntimeException(e);
        }
    }


    private void checkJsonDataAndThrowExceptionIfNecessary(String jsonData) {
        if (StringUtils.isEmpty(jsonData) || "{}".equals(jsonData)) {
            String message = String.format("Empty JSON data!");
            LOG.error(message);
            throw new IllegalStateException(message);
        }
    }
}
