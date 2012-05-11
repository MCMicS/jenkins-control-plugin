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

import java.util.*;

public class BrowserPreferences {

    private Set<String> favorites = new HashSet<String>();
    private String lastSelectedViewName;

    public void addToFavorite(String jobName) {
        if (isNotAFavoriteJob(jobName)) {
            favorites.add(jobName);
        }
    }

    public boolean isNotAFavoriteJob(String jobName) {
        return !favorites.contains(jobName);
    }

    public void clearFavorites() {
        favorites.clear();
    }

    public Set<String> getFavorites() {
        return favorites;
    }

    public void setLastSelectedView(String viewName) {
        this.lastSelectedViewName = viewName;
    }

    public String getLastSelectedView() {
        return lastSelectedViewName;
    }
}
