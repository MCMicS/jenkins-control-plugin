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

package org.codinjutsu.tools.jenkins.view;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.panels.NonOpaquePanel;
import org.codinjutsu.tools.jenkins.BuildStatusSummaryFactory;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsToolWindowFactory;
import org.codinjutsu.tools.jenkins.logic.BuildStatusAggregator;
import org.codinjutsu.tools.jenkins.view.util.BuildStatusIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.Stream;

/**
 * Jenkins status bar widget
 */
public class JenkinsStatusBarWidget extends NonOpaquePanel implements CustomStatusBarWidget {

    private final transient Project project;

    public static JenkinsStatusBarWidget getInstance(Project project) {
        return project.getService(JenkinsStatusBarWidget.class);
    }

    public JenkinsStatusBarWidget(Project project) {
        this.project = project;

        JComponent buildStatusIcon = createStatusIcon(new BuildStatusAggregator());
        setLayout(new BorderLayout());
        add(buildStatusIcon, BorderLayout.CENTER);
    }

    public void updateStatusIcon(final BuildStatusAggregator buildStatusAggregator) {
        ApplicationManager.getApplication().invokeLater(() -> {
            removeAll();

            final JComponent buildIcon = createStatusIcon(buildStatusAggregator);
            add(buildIcon, BorderLayout.CENTER);

            updateUI();
        });

    }

    private JComponent createStatusIcon(BuildStatusAggregator aggregator) {
        final JenkinsAppSettings jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);
        JComponent statusIcon = BuildStatusIcon.createIcon(jenkinsAppSettings.isShowAllInStatusbar(), aggregator, BuildStatusEnumRenderer.getInstance(project));
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                activateBrowserToolWindow();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                activateBrowserToolWindow();
            }
        };
        Stream.concat(Stream.of(statusIcon.getComponents()), Stream.of(statusIcon)).forEach(c -> c.addMouseListener(adapter));
        // Default border is set in com.intellij.openapi.wm.impl.status.IdeStatusBarImpl.wrap
        return statusIcon;
    }

    private void activateBrowserToolWindow() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(JenkinsToolWindowFactory.JENKINS_BROWSER);

        if (toolWindow == null) {
            return;
        }

        toolWindow.activate(null);
    }

    @Override
    @NotNull
    public String ID() {
        return BuildStatusSummaryFactory.BUILD_STATUS_SUMMARY_ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {}

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void dispose() {}
}
