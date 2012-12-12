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

import java.util.LinkedList;
import java.util.List;

public class View {

    public static String FAVORITE = "My Favorites";
    private String name;

    private String url;

    private boolean isNested;

    private final List<View> subViews = new LinkedList<View>();

    public View() {
    }

    protected View(String name, String url, boolean isNested) {
        this.name = name;
        this.url = url;
        this.isNested = isNested;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isNested() {
        return isNested;
    }

    public boolean hasNestedView() {
        return !subViews.isEmpty();
    }

    public List<View> getSubViews() {
        return subViews;
    }

    public void addSubView(View subView) {
        this.subViews.add(subView);
    }

    public static View createView(String viewName, String viewUrl) {
        return new View(viewName, viewUrl, false);
    }

    public static View createNestedView(String viewName, String viewUrl) {
        return new View(viewName, viewUrl, true);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setNested(boolean nested) {
        isNested = nested;
    }
}
