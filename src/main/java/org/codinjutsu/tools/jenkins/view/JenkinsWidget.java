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
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.util.MinimizeButton;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.UIUtil;
import org.codinjutsu.tools.jenkins.logic.BuildStatusAggregator;
import org.codinjutsu.tools.jenkins.view.util.BuildStatusIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JenkinsWidget extends NonOpaquePanel implements CustomStatusBarWidget {

    private StatusBar myStatusBar;

    private final BuildSummaryPanel buildStatusSummaryPanel;

    private JBPopup myPopup;

    public JenkinsWidget(Project project) {
        this.buildStatusSummaryPanel = new BuildSummaryPanel();
        Disposer.register(project, this);

        JComponent buildStatusIcon = createStatusIcon(new BuildStatusAggregator());
        setLayout(new BorderLayout());
        add(buildStatusIcon, BorderLayout.CENTER);
    }

    public void updateInformation(BuildStatusAggregator buildStatusAggregator) {
        buildStatusSummaryPanel.setInformation(buildStatusAggregator);

        final JComponent buildIcon = createStatusIcon(buildStatusAggregator);

        invalidate();
        removeAll();
        add(buildIcon, BorderLayout.CENTER);
        validate();
    }

    private JComponent createStatusIcon(BuildStatusAggregator aggregator) {
        JComponent statusIcon = BuildStatusIcon.createIcon(aggregator);
        statusIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handle(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handle(e);
            }
        });

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(StatusBarWidget.WidgetBorder.INSTANCE);

        return statusIcon;
    }

    private void handle(MouseEvent e) {
        if (myPopup != null && myPopup.isVisible()) {
            if (!myPopup.isFocused()) {
                myPopup.setRequestFocus(true);
            }
            return;

        }
        if (UIUtil.isActionClick(e, MouseEvent.MOUSE_PRESSED)) {
            Point point = new Point(e.getX(), e.getY());
            final Dimension dimension = buildStatusSummaryPanel.getPreferredSize();
            point = new Point(point.x - dimension.width, point.y - dimension.height);
            showPanel(new RelativePoint(e.getComponent(), point));
        }
    }

    private void showPanel(RelativePoint point) {
        myPopup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(buildStatusSummaryPanel, buildStatusSummaryPanel)
                .setMovable(true)
                .setResizable(true)
                .setTitle("Build Status summary")
                .setDimensionServiceKey(null, "JenkinsBuildStatusPopupWindow", true)
                .setMinSize(getMinSize())
                .setCancelOnClickOutside(false)
                .setRequestFocus(false)
                .setBelongsToGlobalPopupStack(true)
                .setLocateByContent(true)
                .setCancelButton(new MinimizeButton("Hide"))
                .createPopup();

        myPopup.show(point);
    }

    private Dimension getMinSize() {
        final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        size.width *= 0.15d;
        size.height *= 0.1d;
        return size;
    }

    @NotNull
    public String ID() {
        return JenkinsWidget.class.getName();
    }

    public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
        return null;
    }

    public void install(@NotNull StatusBar statusBar) {
        this.myStatusBar = statusBar;
    }

    public void dispose() {
        if (myStatusBar != null) {
            myStatusBar.removeWidget(ID());
        }
    }

    public JComponent getComponent() {
        return this;
    }
}
