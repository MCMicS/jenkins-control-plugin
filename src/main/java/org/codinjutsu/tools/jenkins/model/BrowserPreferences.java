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

package org.codinjutsu.tools.jenkins.model;

import org.apache.commons.lang.StringUtils;

public class BrowserPreferences {

    private String favoriteJob = null;
    private String lastSelectedViewName;

    public void setAsFavorite(String jobName) {
        favoriteJob = jobName;
    }

    public boolean isAFavoriteJob(String jobName) {
        return StringUtils.equals(favoriteJob, jobName);
    }

    public void clearFavorite() {
        favoriteJob = null;
    }

    public String getFavoriteJob() {
        return favoriteJob;
    }

    public void setLastSelectedView(String viewName) {
        this.lastSelectedViewName = viewName;
    }

    public String getLastSelectedView() {
        return lastSelectedViewName;
    }
}
