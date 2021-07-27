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
import org.apache.commons.collections.CollectionUtils;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.util.DateUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class JenkinsJsonParser implements JenkinsParser {

    private static final Logger LOG = Logger.getInstance(JenkinsJsonParser.class);

    private final SimpleDateFormat workspaceDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

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
    public Jenkins createWorkspace(String jsonData) {
        checkJsonDataAndThrowExceptionIfNecessary(jsonData);
        final JsonObject jsonObject = parseJson(jsonData);
        final Optional<View> primaryView = Optional.ofNullable((JsonObject) jsonObject.get(PRIMARY_VIEW)).map(this::getView);

        final String description = jsonObject.getStringOrDefault(createJsonKey(SERVER_DESCRIPTION, ""));
        final String jenkinsUrl = getServerUrl(jsonObject);
        final Jenkins jenkins = new Jenkins(description, jenkinsUrl);
        primaryView.ifPresent(jenkins::setPrimaryView);

        final JsonArray viewsObject = (JsonArray) jsonObject.get(VIEWS);
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
        if (buildDate != null && DateUtil.isValidJenkinsDate(buildDate, workspaceDateFormat)) {
            builder.buildDate(DateUtil.parseDate(buildDate, workspaceDateFormat));
        } else {
            builder.buildDate(timestamp);
        }
        Long duration = lastBuildObject.getLong(createJsonKey(BUILD_DURATION));
        if (duration != null) {
            builder.duration(duration);
        }
        // set parameter
        Optional.ofNullable(getActions(lastBuildObject))
                .flatMap(actions -> actions.stream()
                        .filter(action -> isContainParameters((JsonObject) action))
                        .findFirst()
                )
                .ifPresent(action ->
                        builder.buildParameterList(getBuildParameters((JsonObject) action, url))
                );
        return builder.build();
    }

    @NotNull
    private JsonArray getActions(@NotNull JsonObject lastBuildObject) {
        final JsonArray actions = lastBuildObject.getCollection(createJsonKey(ACTIONS));
        if (actions == null) {
            return new JsonArray();
        }
        actions.removeIf(Objects::isNull);
        return actions;
    }

    @NotNull
    private Collection<JsonObject> getActionsParameter(@NotNull JsonObject action) {
        final Collection<JsonObject> parameters = action.getCollection(createJsonKey(PARAMETERS));
        if (parameters == null) {
            return Collections.emptySet();
        }
        parameters.removeIf(Objects::isNull);
        return parameters;
    }

    @NotNull
    private List<BuildParameter> getBuildParameters(JsonObject action, String buildUrl) {
        return getActionsParameter(action).stream()
                .map(parameter -> BuildParameter.of(
                        parameter.getString(createJsonKey("name")),
                        parameter.getString(createJsonKey("value")),
                        buildUrl
                ))
                .collect(Collectors.toList());
    }

    private boolean isContainParameters(JsonObject action) {
        return CollectionUtils.isNotEmpty(getActionsParameter(action));
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
        final String fullDisplayName = getFullDisplayName(jsonObject);
        final String url = jsonObject.getString(createJsonKey(JOB_URL));
        final String color = jsonObject.getStringOrDefault(createJsonKey(JOB_COLOR, null));
        final boolean buildable = getBoolean(jsonObject.getBoolean(createJsonKey(JOB_IS_BUILDABLE)));
        final boolean inQueue = getBoolean(jsonObject.getBoolean(createJsonKey(JOB_IS_IN_QUEUE)));

        JsonArray healths = (JsonArray) jsonObject.get(JOB_HEALTH);
        final Job.JobBuilder jobBuilder = Job.builder().name(name).jobType(jobType)
                .fullName(fullName)
                .displayName(displayName).fullDisplayName(fullDisplayName)
                .color(color).url(url).inQueue(inQueue).buildable(buildable)
                .health(getHealth(healths));

        final EnumSet<BuildType> availableBuildTypes = EnumSet.noneOf(BuildType.class);
        JsonObject lastBuildObject = (JsonObject) jsonObject.get(JOB_LAST_BUILD);
        Optional.ofNullable(lastBuildObject).map(this::getLastBuild).ifPresent(jobBuilder::lastBuild);
        JsonObject lastCompletedBuildObject = (JsonObject) jsonObject.get(JOB_LAST_COMPLETED_BUILD);
        JsonObject lastSuccessfulBuildObject = (JsonObject) jsonObject.get(JOB_LAST_SUCCESSFUL_BUILD);
        JsonObject lastFailedBuildObject = (JsonObject) jsonObject.get(JOB_LAST_FAILED_BUILD);
        addBuildType(availableBuildTypes, BuildType.LAST, lastBuildObject);
        if (!availableBuildTypes.contains(BuildType.LAST)) {
            addBuildType(availableBuildTypes, BuildType.LAST, lastCompletedBuildObject);
        }
        addBuildType(availableBuildTypes, BuildType.LAST_SUCCESSFUL, lastSuccessfulBuildObject);
        addBuildType(availableBuildTypes, BuildType.LAST_FAILED, lastFailedBuildObject);
        jobBuilder.availableBuildTypes(availableBuildTypes);

        JsonArray parameterProperty = (JsonArray) jsonObject.get(PARAMETER_PROPERTY);
        jobBuilder.parameters(getParameters(parameterProperty));
        return jobBuilder.build();
    }

    private void addBuildType(@NotNull Collection<BuildType> buildTypes, @NotNull BuildType buildType,
                              @Nullable JsonObject buildJsonObject) {
        Optional.ofNullable(buildJsonObject).ifPresent(o -> buildTypes.add(buildType));
    }

    @Nullable
    private String getDisplayName(@NotNull JsonObject jsonObject) {
        return jsonObject.getStringOrDefault(createJsonKey(JOB_DISPLAY_NAME));
    }

    @Nullable
    private String getFullDisplayName(@NotNull JsonObject jsonObject) {
        return jsonObject.getStringOrDefault(createJsonKey(JOB_FULL_DISPLAY_NAME));
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
            definitions.removeIf(Objects::isNull);
            for (Object defObj : definitions) {
                jobParameters.add(getJobParameter((JsonObject) defObj));
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
        final JobParameterType jobParameterType = JobParameterType.getType(type, parameterClass);
        jobParameterBuilder.jobParameterType(jobParameterType);
        jobParameterBuilder.choices(getChoices((JsonArray) parameterObj.get(PARAMETER_CHOICE)));
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

    @NotNull
    @Override
    public List<Computer> createComputers(String computerJsonArray) {
        checkJsonDataAndThrowExceptionIfNecessary(computerJsonArray);
        final JsonObject jsonObject = parseJson(computerJsonArray);
        JsonArray jobObjects = (JsonArray) jsonObject.get(COMPUTER);
        return jobObjects.stream().map(object -> (JsonObject) object).map(this::getComputer).collect(
                Collectors.toCollection(LinkedList::new));
    }

    @NotNull
    @Override
    public Computer createComputer(String computerJson) {
        checkJsonDataAndThrowExceptionIfNecessary(computerJson);
        return getComputer(parseJson(computerJson));
    }

    @NotNull
    @Override
    public String getServerUrl(String serverData) {
        checkJsonDataAndThrowExceptionIfNecessary(serverData);
        return getServerUrl(parseJson(serverData));
    }

    @NotNull
    private String getServerUrl(JsonObject jsonObject) {
        final Optional<View> primaryView = Optional.ofNullable((JsonObject) jsonObject.get(PRIMARY_VIEW)).map(this::getView);
        final String primaryViewUrl = primaryView.map(View::getUrl).orElse("");
        return Optional.ofNullable(jsonObject.getStringOrDefault(createJsonKey(SERVER_URL, primaryViewUrl))).orElse(StringUtils.EMPTY);
    }

    @NotNull
    private Computer getComputer(JsonObject computerJson) {
        final String displayName = computerJson.getString(createJsonKey("displayName"));
        final String description = computerJson.getStringOrDefault(createJsonKey("description", StringUtils.EMPTY));
        final boolean offline = computerJson.getBooleanOrDefault(createJsonKey("offline", false));
        final JsonArray labelsJson = (JsonArray) computerJson.get("assignedLabels");
        final Computer.ComputerBuilder computerBuilder = Computer.builder()
                .displayName(displayName)
                .description(description)
                .labels(getComputerLabels(labelsJson))
                .offline(offline);
        return computerBuilder.build();
    }

    @NotNull
    private List<String> getComputerLabels(JsonArray labelsJson) {
        final List<String> labels = new LinkedList<>();
        if (labelsJson == null || labelsJson.isEmpty()) {
            return labels;
        }

        for (Object obj : labelsJson) {
            JsonObject labelJson = (JsonObject) obj;
            final String nameProperty = "name";
            if (labelJson == null || labelJson.isEmpty() || !labelJson.containsKey(nameProperty)) {
                continue;
            }
            labels.add(labelJson.getString(createJsonKey(nameProperty)));
        }
        return labels;
    }


    private void checkJsonDataAndThrowExceptionIfNecessary(String jsonData) {
        if (StringUtils.isEmpty(jsonData) || "{}".equals(jsonData)) {
            String message = "Empty JSON data!";
            LOG.error(message);
            throw new IllegalStateException(message);
        }
    }
}
