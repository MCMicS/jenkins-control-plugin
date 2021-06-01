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

package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.logic.*;
import org.codinjutsu.tools.jenkins.view.JenkinsWidget;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class JenkinsWindowManager implements Disposable {

    private final Project project;

    @NotNull
    public static Optional<JenkinsWindowManager> getInstance(Project project) {
        return Optional.ofNullable(project.getService(JenkinsWindowManager.class));
    }

    public JenkinsWindowManager(Project project) {
        this.project = project;
    }

    public void reloadConfiguration() {
        LoginService.getInstance(project).performAuthentication();
    }

    @Override
    public void dispose() {
        RssAuthenticationActionHandler.getInstance(project).dispose();
        BrowserPanelAuthenticationHandler.getInstance(project).dispose();
        JenkinsWidget.getInstance(project).dispose();
        ExecutorService.getInstance(project).dispose();
        RequestManager.getInstance(project).dispose();
    }
}
