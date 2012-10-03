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

import org.codinjutsu.tools.jenkins.model.Job;
import org.jdom.Document;
import org.jdom.Element;

import java.util.LinkedList;
import java.util.List;


class LoadClassicViewStrategy implements LoadViewStrategy {
    public List<Job> loadJenkinsView(Document document) {
        List<Element> jobElements = document.getRootElement().getChildren(JenkinsRequestManager.JOB);
        return JenkinsBrowserLogic.createJobs(jobElements);
    }
}
