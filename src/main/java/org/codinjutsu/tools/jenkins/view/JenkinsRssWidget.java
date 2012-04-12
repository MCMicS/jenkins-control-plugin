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

package org.codinjutsu.tools.jenkins.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.util.BuildStatusIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class JenkinsRssWidget extends JPanel implements CustomStatusBarWidget, StatusBarWidget.Multiframe {

    private final Project project;
    private StatusBar myStatusBar;

    public JenkinsRssWidget(Project project) {
        this.project = project;

        setOpaque(false);

        init();
    }

    private void init() {
        BuildStatusIcon buildStatusIcon = createStatusIcon(0);
        setLayout(new BorderLayout());
        add(buildStatusIcon, BorderLayout.CENTER);
    }

    private BuildStatusIcon createStatusIcon(int remainingBrokenBuilds) {
        BuildStatusIcon buildStatusIcon = BuildStatusIcon.createIcon(remainingBrokenBuilds);
        buildStatusIcon.setBorder(WidgetBorder.INSTANCE);
        return buildStatusIcon;
    }

    public void updateIcon(int nbRemainingBrokenBuilds) {
        final BuildStatusIcon buildIcon = createStatusIcon(nbRemainingBrokenBuilds);
        GuiUtil.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                invalidate();
                removeAll();
                add(buildIcon, BorderLayout.CENTER);
                validate();
            }
        });
    }

    @NotNull
    public String ID() {
        return JenkinsRssWidget.class.getName();
    }

    public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
        return null;
    }

    public void install(@NotNull StatusBar statusBar) {
        this.myStatusBar = statusBar;
    }

    public void dispose() {
        myStatusBar = null;
    }

    public StatusBarWidget copy() {
        return new JenkinsRssWidget(project);
    }

    public JComponent getComponent() {
        return this;
    }
}
