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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.List;

class LoadCloudbeesViewStrategy implements LoadViewStrategy {
    public List<Job> loadJenkinsView(Document document) {
        List<Element> viewElements = document.getRootElement().getChildren(RequestManager.VIEW);
        if (viewElements.isEmpty()) {
            return Collections.emptyList();
        }

        Element viewElement = viewElements.get(0);
        List<Element> jobElements = viewElement.getChildren(RequestManager.JOB);

        return XmlRequestManager.createJobs(jobElements);
    }

    @Override
    public List<Job> loadJenkinsView(JSONObject jsonObject) {
        JSONArray jsonArray1 = (JSONArray) jsonObject.get(RequestManager.VIEWS);
        if (jsonArray1 != null && !jsonArray1.isEmpty()) {
            JSONArray jsonArray = (JSONArray) ((JSONObject) jsonArray1.get(0)).get(RequestManager.JOBS);
            return JsonRequestManager.createJobs(jsonArray);
        } else {
            return Collections.emptyList();
        }
    }
}
