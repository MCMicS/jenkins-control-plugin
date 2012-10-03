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
    Jenkins loadJenkinsWorkspace(JenkinsConfiguration configuration);

    Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsConfiguration configuration);

    List<Job> loadJenkinsView(String viewUrl);

    Job loadJob(String jenkinsJobUrl);

    void runBuild(Job job, JenkinsConfiguration configuration);

    void runParameterizedBuild(Job job, JenkinsConfiguration configuration, Map<String, String> paramValueMap);

    void authenticate(String serverUrl, SecurityMode securityMode, String username, String passwordFile, String crumbDataFile);

    List<Job> loadFavoriteJobs(List<JenkinsConfiguration.FavoriteJob> favoriteJobs);
}
