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

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.util.DateUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JenkinsJsonParser implements JenkinsParser {

    private static final Logger LOG = Logger.getLogger(JenkinsJsonParser.class);

    private static boolean getBoolean(Object obj) {
        return Boolean.TRUE.equals(obj);
    }

    private static JsonObject parseJson(String jsonData) {
        return Jsoner.deserialize(jsonData, new JsonObject());
    }

    @NotNull
    private static JsonKey createJsonKey(String key) {
        return createJsonKey(key, null);
    }

    @NotNull
    private static JsonKey createJsonKey(String key, Object value) {
        return Jsoner.mintJsonKey(key, value);
    }

    @Override
    public Jenkins createWorkspace(String jsonData, String serverUrl) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        Jenkins jenkins = new Jenkins("", serverUrl);
        final JsonObject jsonObject = parseJson(jsonData);
        JsonObject primaryViewObject = (JsonObject) jsonObject.get(PRIMARY_VIEW);
        if (primaryViewObject != null) {
            jenkins.setPrimaryView(getView(primaryViewObject));
        }

        JsonArray viewsObject = (JsonArray) jsonObject.get(VIEWS);
        if (viewsObject != null) {
            jenkins.setViews(getViews(viewsObject));
        }
        return jenkins;
    }

    private List<View> getViews(JsonArray viewsObjects) {
        List<View> views = new LinkedList<>();
        for (Object obj : viewsObjects) {
            JsonObject viewObject = (JsonObject) obj;
            views.add(getView(viewObject));
        }

        return views;
    }

    private View getView(JsonObject viewObject) {
        final View.ViewBuilder<?, ?> viewBuilder = View.builder();
        viewBuilder.isNested(false);
        final String unknownViewName = "Unknown";
        viewBuilder.name(viewObject.getStringOrDefault(createJsonKey(VIEW_NAME, unknownViewName)));
        String url = viewObject.getString(createJsonKey(VIEW_URL));
        viewBuilder.url(url);

        JsonArray subViewObjs = (JsonArray) viewObject.get(VIEWS);
        if (subViewObjs != null) {
            for (Object obj : subViewObjs) {
                JsonObject subviewObj = (JsonObject) obj;

                final View.ViewBuilder<?, ?> nestedViewBuilder = View.builder();
                nestedViewBuilder.isNested(true);

                String currentName = subviewObj.getStringOrDefault(createJsonKey(VIEW_NAME, unknownViewName));
                nestedViewBuilder.name(currentName);

                String subViewUrl = subviewObj.getString(createJsonKey(VIEW_URL));
                nestedViewBuilder.url(subViewUrl);

                viewBuilder.subView(nestedViewBuilder.build());
            }
        }
        return viewBuilder.build();
    }

    @Override
    public Job createJob(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        return getJob(parseJson(jsonData));

    }

    @NotNull
    public Build createBuild(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        return getBuild(parseJson(jsonData));
    }

    @Override
    public List<Build> createBuilds(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        final JsonObject jsonObject = parseJson(jsonData);
        final JsonArray buildsObject = jsonObject.getCollectionOrDefault(createJsonKey(BUILDS, new JsonArray()));
        return Optional.ofNullable(buildsObject).map(this::getBuilds).orElse(new ArrayList<>());
    }

    @NotNull
    private Build getBuild(@Nullable JsonObject lastBuildObject) {
        if (lastBuildObject == null) {
            return Build.NULL;
        }

        final Build.BuildBuilder builder = Build.builder();
        builder.building(getBoolean(lastBuildObject.getBoolean(createJsonKey((BUILD_IS_BUILDING)))));
        final OptionalInt number = OptionalInt.of(lastBuildObject.getInteger(createJsonKey(BUILD_NUMBER)));
        builder.number(number.orElse(0));
        final String status = lastBuildObject.getStringOrDefault(createJsonKey(BUILD_RESULT, BuildStatusEnum.NULL.getStatus()));
        builder.status(BuildStatusEnum.parseStatus(status));
        final String url = lastBuildObject.getString(createJsonKey(BUILD_URL));
        builder.url(url);
        final Long timestampMillis = lastBuildObject.getLong(createJsonKey(BUILD_TIMESTAMP));
        final Date timestamp;
        if (timestampMillis == null) {
            timestamp = new Date();
        } else {
            timestamp = new Date(timestampMillis);
        }
        builder.timestamp(timestamp);
        builder.displayName(lastBuildObject.getString(createJsonKey(BUILD_DISPLAY_NAME)));
        builder.fullDisplayName(lastBuildObject.getString(createJsonKey(BUILD_FULL_DISPLAY_NAME)));

        final String buildDate = lastBuildObject.getString(createJsonKey(BUILD_ID));
        // BUILD_ID
        //    Die aktuelle Build-ID. In Builds ab Jenkins 1.597 ist dies die Build-Nummer, vorher ein Zeitstempel im Format YYYY-MM-DD_hh-mm-ss.
        if (buildDate != null && DateUtil.isValidJenkinsDate(buildDate)) {
            builder.buildDate(DateUtil.parseDate(buildDate, DateUtil.WORKSPACE_DATE_FORMAT));
        } else {
            builder.buildDate(timestamp);
        }
        Long duration = lastBuildObject.getLong(createJsonKey(BUILD_DURATION));
        if (duration != null) {
            builder.duration(duration);
        }

        return builder.build();
    }

    @NotNull
    private List<Build> getBuilds(@NotNull JsonArray buildsObjects) {
        List<Build> builds = new LinkedList<>();
        for (Object obj : buildsObjects) {
            JsonObject buildObject = (JsonObject) obj;
            builds.add(getBuild(buildObject));
        }
        return builds;
    }

    @NotNull
    private JobType getJobTypeByJenkinsClass(@NotNull String className, @NotNull JobType defaultValue) {
        final JobType jobType;
        if (className.endsWith("WorkflowMultiBranchProject")) {
            jobType = JobType.MULTI_BRANCH;
        } else if (className.endsWith("Folder")) {
            jobType = JobType.FOLDER;
        } else if (className.endsWith(Job.WORKFLOW_JOB)) {
            jobType = JobType.JOB;
        } else {
            jobType = defaultValue;
        }
        return jobType;
    }

    @NotNull
    private JobType getJobType(@NotNull JsonObject jobObject) {
        final JobType jobType;
        final String jenkinsType = jobObject.getStringOrDefault(createJsonKey(CLASS, Job.WORKFLOW_JOB));
        final JobType defaultValue;
        if (jobObject.containsKey(JOBS)) {
            defaultValue = JobType.FOLDER;
        } else {
            defaultValue = JobType.JOB;
        }
        jobType = getJobTypeByJenkinsClass(jenkinsType, defaultValue);
        return jobType;
    }

    @NotNull
    private Job getJob(JsonObject jsonObject) {
        final String name = jsonObject.getString(createJsonKey(JOB_NAME));
        final String fullName = jsonObject.getStringOrDefault(createJsonKey(JOB_FULL_NAME, name));
        final JobType jobType = getJobType(jsonObject);
        final String displayName = getDisplayName(jsonObject);
        final String url = jsonObject.getString(createJsonKey(JOB_URL));
        final String color = jsonObject.getStringOrDefault(createJsonKey(JOB_COLOR, null));
        final boolean buildable = getBoolean(jsonObject.getBoolean(createJsonKey(JOB_IS_BUILDABLE)));
        final boolean inQueue = getBoolean(jsonObject.getBoolean(createJsonKey(JOB_IS_IN_QUEUE)));

        JsonArray healths = (JsonArray) jsonObject.get(JOB_HEALTH);
        final Job.JobBuilder jobBuilder = Job.builder().name(name).jobType(jobType).displayName(displayName)
                .fullName(fullName).color(color).url(url).inQueue(inQueue).buildable(buildable)
                .health(getHealth(healths));

        JsonObject lastBuildObject = (JsonObject) jsonObject.get(JOB_LAST_BUILD);
        Optional.ofNullable(lastBuildObject).map(this::getLastBuild).ifPresent(jobBuilder::lastBuild);
        JsonArray parameterProperty = (JsonArray) jsonObject.get(PARAMETER_PROPERTY);
        jobBuilder.parameters(getParameters(parameterProperty));
        return jobBuilder.build();
    }

    @Nullable
    private String getDisplayName(@NotNull JsonObject jsonObject) {
        final JsonKey preferFullDisplayName = createJsonKey(JOB_FULL_DISPLAY_NAME,
                jsonObject.getString(createJsonKey(JOB_DISPLAY_NAME)));
        return jsonObject.getStringOrDefault(preferFullDisplayName);
    }

    @NotNull
    private List<JobParameter> getParameters(JsonArray parameterProperties) {
        List<JobParameter> jobParameters = new LinkedList<>();
        if (parameterProperties == null || parameterProperties.isEmpty()) {
            return jobParameters;
        }

        for (Object obj : parameterProperties) {
            JsonObject parameterProperty = (JsonObject) obj;
            if (parameterProperty == null || parameterProperty.isEmpty()) {
                continue;
            }

            final JsonArray definitions = parameterProperty.getCollectionOrDefault(createJsonKey(PARAMETER_DEFINITIONS,
                    new JsonArray()));
            for (Object defObj : definitions) {
                JsonObject parameterObj = (JsonObject) defObj;
                jobParameters.add(getJobParameter(parameterObj));
            }
        }
        return jobParameters;
    }

    @NotNull
    private JobParameter getJobParameter(JsonObject parameterObj) {
        final JobParameter.JobParameterBuilder jobParameterBuilder = JobParameter.builder();
        String name = parameterObj.getString(createJsonKey(PARAMETER_NAME));
        jobParameterBuilder.name(name);
        JsonObject defaultParamObj = (JsonObject) parameterObj.get(PARAMETER_DEFAULT_PARAM);
        if (defaultParamObj != null && !defaultParamObj.isEmpty()) {
            Optional<String> defaultValue = Optional.ofNullable(defaultParamObj.get(PARAMETER_DEFAULT_PARAM_VALUE))
                    .map(Object::toString);
            defaultValue.ifPresent(jobParameterBuilder::defaultValue);
        }

        String description = parameterObj.getString(createJsonKey(PARAMETER_DESCRIPTION));
        if (description != null && !description.isEmpty()) {
            jobParameterBuilder.description(description);
        }

        String type = parameterObj.getStringOrDefault(createJsonKey(PARAMETER_TYPE, StringUtils.EMPTY));
        String parameterClass = parameterObj.getString(createJsonKey(CLASS));
        jobParameterBuilder.jobParameterType(JobParameterType.getType(type, parameterClass));
        JsonArray choices = (JsonArray) parameterObj.get(PARAMETER_CHOICE);
        jobParameterBuilder.choices(getChoices(choices));
        return jobParameterBuilder.build();
    }

    private List<String> getChoices(JsonArray choiceObjs) {
        List<String> choices = new LinkedList<>();
        if (choiceObjs == null || choiceObjs.isEmpty()) {
            return choices;
        }
        for (Object choiceObj : choiceObjs) {
            choices.add(String.valueOf(choiceObj));
        }
        return choices;
    }

    @NotNull
    private Build getLastBuild(JsonObject lastBuildObject) {
        return getBuild(lastBuildObject);
    }

    @Nullable
    private Job.Health getHealth(JsonArray healths) {
        if (healths == null || healths.isEmpty()) {
            return null;
        }

        JsonObject healthObject = (JsonObject) healths.get(0);
        String description = healthObject.getString(createJsonKey(JOB_HEALTH_DESCRIPTION));
        String healthLevel = healthObject.getString(createJsonKey(JOB_HEALTH_ICON));
        if (StringUtils.isNotEmpty(healthLevel)) {
            if (healthLevel.endsWith(".png"))
                healthLevel = healthLevel.substring(0, healthLevel.lastIndexOf(".png"));
            else {
                healthLevel = healthLevel.substring(0, healthLevel.lastIndexOf(".gif"));
            }
        } else {
            healthLevel = null;
        }
        if (StringUtils.isEmpty(healthLevel)) {
            return null;
        } else {
            return new Job.Health(healthLevel, description);
        }
    }

    @Override
    public List<Job> createJobs(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        List<Job> jobs = new LinkedList<>();
        final JsonObject jsonObject = parseJson(jsonData);
        JsonArray jobObjects = (JsonArray) jsonObject.get(JOBS);
        for (Object object : jobObjects) {
            JsonObject jobObject = (JsonObject) object;
            jobs.add(getJob(jobObject));
        }

        return jobs;
    }

    @Override
    public List<Job> createCloudbeesViewJobs(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        List<Job> jobs = new LinkedList<>();
        final JsonObject jsonObject = parseJson(jsonData);
        JsonArray viewObjs = (JsonArray) jsonObject.get(VIEWS);
        if (viewObjs == null || viewObjs.isEmpty()) {
            return jobs;
        }

        JsonObject viewJobObj = (JsonObject) viewObjs.get(0);
        if (viewJobObj == null) {
            return jobs;
        }

        JsonArray jobObjs = (JsonArray) viewJobObj.get(JOBS);
        for (Object obj : jobObjs) {
            JsonObject jobObj = (JsonObject) obj;
            jobs.add(getJob(jobObj));
        }

        return jobs;
    }


    private void checkJsonDataAndThrowExceptionIfNecessary(String jsonData) {
        if (StringUtils.isEmpty(jsonData) || "{}".equals(jsonData)) {
            String message = "Empty JSON data!";
            LOG.error(message);
            throw new IllegalStateException(message);
        }
    }
}
