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

import com.google.gson.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class JenkinsJsonParser implements JenkinsParser {

    private static final Logger LOG = Logger.getLogger(JenkinsJsonParser.class);

    private static boolean getBoolean(@Nullable JsonElement jsonElement) {
        return !isNull(jsonElement) && jsonElement.getAsBoolean();
    }

    @NotNull
    private static JsonObject parseJson(@Nullable String jsonData) {
        final Gson parser = new Gson();
        return parser.fromJson(jsonData, JsonObject.class);
    }

    private static String createParserErrorMessage(String jsonData) {
        return String.format("Error during parsing JSON data : %s", jsonData);
    }

    @Nullable
    private static String getAsStringOrNull(@Nullable JsonElement jsonElement) {
        return getAsStringOrDefault(jsonElement, () -> null);
    }

    @Nullable
    private static String getAsStringOrEmpty(@Nullable JsonElement jsonElement) {
        return getAsStringOrDefault(jsonElement, () -> "");
    }

    @Nullable
    private static String getAsStringOrDefault(@Nullable JsonElement jsonElement, @NotNull Supplier<String> defaultSupplier) {
        return isNull(jsonElement) ? defaultSupplier.get() : jsonElement.getAsString();
    }

    private static boolean isEmpty(@NotNull JsonObject jsonObject) {
        return jsonObject.entrySet().isEmpty();
    }

    private static boolean isEmpty(@NotNull JsonArray jsonArray) {
        return jsonArray.size() == 0;
    }

    private static boolean isNull(@Nullable JsonElement jsonElement) {
        return jsonElement == null || jsonElement.isJsonNull();
    }

    @Override
    public Jenkins createWorkspace(String jsonData, String serverUrl) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        Jenkins jenkins = new Jenkins("", serverUrl);

        try {
            final JsonObject jsonObject = parseJson(jsonData);
            Optional.ofNullable(jsonObject.getAsJsonObject(PRIMARY_VIEW))//
                    .map(this::getView).ifPresent(jenkins::setPrimaryView);
            Optional.ofNullable(jsonObject.getAsJsonArray(VIEWS))//
                    .map(this::getViews).ifPresent(jenkins::setViews);
        } catch (JsonParseException e) {
            LOG.error(createParserErrorMessage(jsonData), e);
            throw new RuntimeException(e);
        }
        return jenkins;
    }

    private List<View> getViews(JsonArray viewsObjects) {
        List<View> views = new LinkedList<>();
        for (JsonElement element : viewsObjects) {
            views.add(getView(element.getAsJsonObject()));
        }

        return views;
    }

    private View getView(JsonObject viewObject) {
        View view = new View();
        view.setNested(false);
        JsonElement name = viewObject.get(VIEW_NAME);
        if (name != null) {
            view.setName(name.getAsString());
        }

        JsonElement url = viewObject.get(VIEW_URL);
        if (name != null) {
            view.setUrl(url.getAsString());
        }

        JsonArray subViewObjs = viewObject.getAsJsonArray(VIEWS);
        if (subViewObjs != null) {
            for (JsonElement jsonElement : subViewObjs) {
                final JsonObject subviewObj = jsonElement.getAsJsonObject();
                View nestedView = new View();
                nestedView.setNested(true);

                JsonElement currentName = subviewObj.get(VIEW_NAME);
                nestedView.setName(currentName.getAsString());

                JsonElement subViewUrl = subviewObj.get(VIEW_URL);
                nestedView.setUrl(subViewUrl.getAsString());

                view.addSubView(nestedView);
            }
        }
        return view;
    }

    @Override
    public Job createJob(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        try {
            return getJob(parseJson(jsonData));
        } catch (JsonParseException e) {
            LOG.error(createParserErrorMessage(jsonData), e);
            throw new RuntimeException(e);
        }

    }

    @NotNull
    public Build createBuild(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        try {
            return getBuild(parseJson(jsonData));
        } catch (JsonParseException e) {
            LOG.error(createParserErrorMessage(jsonData), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Build> createBuilds(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);

        try {
            final JsonObject jsonObject = parseJson(jsonData);
            JsonArray buildsObject = jsonObject.getAsJsonArray(BUILDS);
            return Optional.ofNullable(buildsObject).map(this::getBuilds).orElse(new ArrayList<>());
        } catch (JsonParseException e) {
            LOG.error(createParserErrorMessage(jsonData), e);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private Build getBuild(@Nullable JsonElement jsonElement) {
        if (isNull(jsonElement)) {
            return new Build();
        }

        final JsonObject lastBuildObject = jsonElement.getAsJsonObject();
        final Build build = new Build();
        JsonElement buildDate = lastBuildObject.get(BUILD_ID);
        build.setBuildDate(buildDate.getAsString());
        final boolean building = getBoolean(lastBuildObject.get(BUILD_IS_BUILDING));
        build.setBuilding(building);
        JsonElement number = lastBuildObject.get(BUILD_NUMBER);
        build.setNumber(number.getAsInt());
        String status = getAsStringOrDefault(lastBuildObject.get(BUILD_RESULT), BuildStatusEnum.NULL::getStatus);
        build.setStatus(status);
        JsonElement url = lastBuildObject.get(BUILD_URL);
        build.setUrl(url.getAsString());
        JsonElement timestamp = lastBuildObject.get(BUILD_TIMESTAMP);
        if (null != timestamp) {
            build.setTimestamp(timestamp.getAsLong());
        }
        JsonElement duration = lastBuildObject.get(BUILD_DURATION);
        if (null != duration) {
            build.setDuration(duration.getAsLong());
        }

        return build;
    }

    @NotNull
    private List<Build> getBuilds(@NotNull JsonArray buildsObjects) {
        List<Build> builds = new LinkedList<>();
        for (JsonElement jsonElement : buildsObjects) {
            if (!isNull(jsonElement)) {
                builds.add(getBuild(jsonElement.getAsJsonObject()));
            }
        }
        return builds;
    }

    private Job getJob(JsonObject jsonObject) {
        final String name = getAsStringOrEmpty(jsonObject.get(JOB_NAME));
        final String displayName = getAsStringOrNull(jsonObject.get(JOB_DISPLAY_NAME));
        final String url = getAsStringOrNull(jsonObject.get(JOB_URL));
        final String color = getAsStringOrNull(jsonObject.get(JOB_COLOR));
        final boolean buildable = getBoolean(jsonObject.get(JOB_IS_BUILDABLE));
        final boolean inQueue = getBoolean(jsonObject.get(JOB_IS_IN_QUEUE));
        final Job job = Job.createJob(name, displayName, color, url, inQueue, buildable);
        JsonArray healths = jsonObject.getAsJsonArray(JOB_HEALTH);
        job.setHealth(getHealth(healths));

        JsonElement lastBuildObject = jsonObject.get(JOB_LAST_BUILD);
        job.setLastBuild(getLastBuild(lastBuildObject));
        JsonArray parameterProperty = jsonObject.getAsJsonArray(PARAMETER_PROPERTY);
        job.addParameters(getParameters(parameterProperty));
        return job;
    }

    private List<JobParameter> getParameters(JsonArray parameterProperties) {
        List<JobParameter> jobParameters = new LinkedList<>();
        if (parameterProperties == null || isEmpty(parameterProperties)) {
            return jobParameters;
        }

        for (JsonElement jsonParameterProperty : parameterProperties) {
            final JsonObject parameterProperty = jsonParameterProperty.getAsJsonObject();
            if (parameterProperty == null || isEmpty(parameterProperty)) {
                continue;
            }

            JsonArray definitions = parameterProperty.getAsJsonArray(PARAMETER_DEFINITIONS);
            if (definitions == null) {
                continue;
            }
            for (JsonElement jsonDefinition : definitions) {
                final JsonObject parameterObj = jsonDefinition.getAsJsonObject();
                final JobParameter jobParameter = new JobParameter();
                final JsonObject defaultParamObj = parameterObj.getAsJsonObject(PARAMETER_DEFAULT_PARAM);
                if (defaultParamObj != null && !isEmpty(defaultParamObj)) {
                    Object defaultValue = defaultParamObj.get(PARAMETER_DEFAULT_PARAM_VALUE);
                    if (defaultValue != null) {
                        jobParameter.setDefaultValue(defaultValue.toString());
                    }
                }

                JsonElement name = parameterObj.get(PARAMETER_NAME);
                jobParameter.setName(name.getAsString());

                String description = getAsStringOrNull(parameterObj.get(PARAMETER_DESCRIPTION));
                if (description != null && !description.isEmpty()) {
                    jobParameter.setDescription(description);
                }

                JsonElement type = parameterObj.get(PARAMETER_TYPE);
                jobParameter.setType(type.getAsString());
                JsonArray choices = parameterObj.getAsJsonArray(PARAMETER_CHOICE);
                jobParameter.setChoices(getChoices(choices));

                jobParameters.add(jobParameter);
            }
        }
        return jobParameters;
    }

    private List<String> getChoices(JsonArray choiceObjs) {
        List<String> choices = new LinkedList<>();
        if (choiceObjs == null || isEmpty(choiceObjs)) {
            return choices;
        }
        for (JsonElement choiceObj : choiceObjs) {
            choices.add(choiceObj.getAsString());
        }
        return choices;
    }

    @NotNull
    private Build getLastBuild(@Nullable JsonElement lastBuildObject) {
        return getBuild(lastBuildObject);
    }

    private Job.Health getHealth(JsonArray healths) {
        if (healths == null || isEmpty(healths)) {
            return null;
        }

        Job.Health health = new Job.Health();
        JsonObject healthObject = healths.get(0).getAsJsonObject();
        String description = getAsStringOrNull(healthObject.get(JOB_HEALTH_DESCRIPTION));
        health.setDescription(description);
        String healthLevel = getAsStringOrNull(healthObject.get(JOB_HEALTH_ICON));
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

        try {
            List<Job> jobs = new LinkedList<>();
            final JsonObject jsonObject = parseJson(jsonData);
            JsonArray jobObjects = jsonObject.getAsJsonArray(JOBS);
            for (JsonElement jsonElement : jobObjects) {
                jobs.add(getJob(jsonElement.getAsJsonObject()));
            }

            return jobs;
        } catch (JsonParseException e) {
            LOG.error(createParserErrorMessage(jsonData), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Job> createCloudbeesViewJobs(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        try {
            List<Job> jobs = new LinkedList<>();
            JsonObject jsonObject = parseJson(jsonData);
            JsonArray viewObjs = jsonObject.getAsJsonArray(VIEWS);
            if (viewObjs == null && isEmpty(viewObjs)) {
                return jobs;
            }

            JsonElement viewJobObj = viewObjs.get(0);
            if (viewJobObj == null) {
                return jobs;
            }

            JsonArray jobObjs = viewJobObj.getAsJsonObject().getAsJsonArray(JOBS);
            for (JsonElement jsonElement : jobObjs) {
                jobs.add(getJob(jsonElement.getAsJsonObject()));
            }

            return jobs;
        } catch (JsonParseException e) {
            LOG.error(createParserErrorMessage(jsonData), e);
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
