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
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Alarm;
import com.intellij.util.ui.UIUtil;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.util.BuildStatusIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JenkinsRssWidget extends JPanel implements CustomStatusBarWidget, StatusBarWidget.Multiframe {

    private final Project project;
    private final RssLatestBuildPanel rssLatestJobPanel;
    private final JPanel myRefreshAndInfoPanel = new JPanel();
    private StatusBar myStatusBar;

    private JBPopup myPopup;

    public JenkinsRssWidget(Project project, RssLatestBuildPanel rssLatestBuildPanel) {
        this.project = project;
        this.rssLatestJobPanel = rssLatestBuildPanel;
        rssLatestBuildPanel.setMinimumSize(new Dimension(100, 100));
        rssLatestBuildPanel.setMaximumSize(new Dimension(150, 300));

        setOpaque(false);


        init();
    }

    private void init() {
        myRefreshAndInfoPanel.setLayout(new BorderLayout());
        myRefreshAndInfoPanel.setOpaque(false);
        myRefreshAndInfoPanel.add(rssLatestJobPanel, BorderLayout.CENTER);

        BuildStatusIcon buildIcon = createStatusIcon(0);
        setLayout(new BorderLayout());
        add(buildIcon, BorderLayout.CENTER);
    }

    private BuildStatusIcon createStatusIcon(int remainingBrokenBuilds) {
        BuildStatusIcon buildStatusIcon = BuildStatusIcon.createIcon(remainingBrokenBuilds);
        buildStatusIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handle(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handle(e);
            }
        });

        buildStatusIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
                repaint();
            }
        });
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
            final Dimension dimension = rssLatestJobPanel.getPreferredSize();
            point = new Point(point.x - dimension.width, point.y - dimension.height);
            showRssPanel(new RelativePoint(e.getComponent(), point));
        }
    }

    public void showRssPanel(RelativePoint point) {
        myPopup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(rssLatestJobPanel, rssLatestJobPanel)
                .setMovable(true)
                .setResizable(true)
                .setTitle("RSS Latest Builds")
                .setDimensionServiceKey(null, "JenkinsRssPopupWindow", true)
                .setMinSize(getMinSize())
                .setCancelOnClickOutside(false)
                .setRequestFocus(false)
                .setBelongsToGlobalPopupStack(true)
                .setLocateByContent(true)
                .setCancelButton(new MinimizeButton("Hide"))
                .createPopup();

        myPopup.showInScreenCoordinates(rssLatestJobPanel, new Point(point.getPoint()));
    }

    private Dimension getMinSize() {
        final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        size.width *= 0.1d;
        size.height *= 0.1d;
        return size;
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
        return new JenkinsRssWidget(project, rssLatestJobPanel);
    }

    public JComponent getComponent() {
        return this;
    }
}
