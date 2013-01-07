package org.codinjutsu.tools.jenkins.logic;

import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;

import java.util.List;

public interface JenkinsParser {
    String JOBS = "jobs";
    String JOB_NAME = "name";
    String JOB_HEALTH = "healthReport";
    String JOB_HEALTH_ICON = "iconUrl";
    String JOB_HEALTH_DESCRIPTION = "description";
    String JOB_URL = "url";
    String JOB_COLOR = "color";
    String JOB_LAST_BUILD = "lastBuild";
    String JOB_IS_BUILDABLE = "buildable";
    String JOB_IS_IN_QUEUE = "inQueue";
    String VIEWS = "views";
    String PRIMARY_VIEW = "primaryView";
    String VIEW_NAME = "name";
    String VIEW_URL = "url";
    String BUILD_IS_BUILDING = "building";
    String BUILD_ID = "id";
    String BUILD_RESULT = "result";
    String BUILD_URL = "url";
    String BUILD_NUMBER = "number";
    String PARAMETER_PROPERTY = "property";
    String PARAMETER_DEFINITIONS = "parameterDefinitions";
    String PARAMETER_NAME = "name";
    String PARAMETER_TYPE = "type";
    String PARAMETER_DEFAULT_PARAM = "defaultParameterValue";
    String PARAMETER_DEFAULT_PARAM_VALUE = "value";
    String PARAMETER_CHOICE = "choices";

    Jenkins createWorkspace(String jsonData, String serverUrl);

    Job createJob(String jsonData);

    List<Job> createViewJobs(String jsonData);

    List<Job> createCloudbeesViewJobs(String jsonData);
}
