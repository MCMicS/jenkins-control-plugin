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

import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.security.SecurityMode;

import java.util.List;
import java.util.Map;

public interface RequestManager {

    String JOB = "job";
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
    String VIEW = "view";
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
    String PARAMETER_DEFINITION = "parameterDefinition";
    String PARAMETER_DEFINITIONS = "parameterDefinitions";
    String PARAMETER_NAME = "name";
    String PARAMETER_TYPE = "type";
    String PARAMETER_DEFAULT_PARAM = "defaultParameterValue";
    String PARAMETER_DEFAULT_PARAM_VALUE = "value";
    String PARAMETER_CHOICE = "choice";
    String RSS_ENTRY = "entry";
    String RSS_TITLE = "title";
    String RSS_LINK = "link";
    String RSS_LINK_HREF = "href";
    String RSS_PUBLISHED = "published";
    String JENKINS_ROOT_TAG = "jenkins";
    String HUDSON_ROOT_TAG = "hudson";
    String FOLDER_ROOT_TAG = "folder";
    String JENKINS_DESCRIPTION = "description";
    String BUILDHIVE_CLOUDBEES = "buildhive";


    Jenkins loadJenkinsWorkspace(JenkinsConfiguration configuration);

    Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsConfiguration configuration);

    List<Job> loadJenkinsView(String viewUrl);

    Job loadJob(String jenkinsJobUrl);

    void runBuild(Job job, JenkinsConfiguration configuration);

    void runParameterizedBuild(Job job, JenkinsConfiguration configuration, Map<String, String> paramValueMap);

    void authenticate(String serverUrl, SecurityMode securityMode, String username, String passwordFile, String crumbDataFile);

    List<Job> loadFavoriteJobs(List<JenkinsConfiguration.FavoriteJob> favoriteJobs);

    void setJenkinsPlateform(JenkinsPlateform cloudbees);
}
