/*
 * Copyright (c) 2011 David Boissier
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

import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.SwingUtils;
import org.codinjutsu.tools.jenkins.view.action.ThreadFunctor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Map.Entry;

import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.SUCCESS;

public class RssLatestJobPanel {
    private JPanel rssContentPanel;
    private JPanel rootPanel;

    public RssLatestJobPanel() {
        rssContentPanel.setLayout(new BoxLayout(rssContentPanel, BoxLayout.Y_AXIS));
    }


    public void cleanRssEntries() {
        SwingUtils.runInSwingThread(new ThreadFunctor() {
            public void run() {
                rssContentPanel.invalidate();

                Component[] components = rssContentPanel.getComponents();
                for (Component component : components) {
                    if (component instanceof BuildResultPanel) {
                        rssContentPanel.remove(component);
                    }
                }

                rssContentPanel.validate();
                rssContentPanel.repaint();

            }
        });
    }


    public void addFinishedBuild(final Map<String, Build> jobBuildByJobNameMap) {

        final RssCallback rssCallback = new RssCallback() {

            public void onClearJobEntry(String jobName) {
                jobBuildByJobNameMap.remove(jobName);
            }
        };

        SwingUtils.runInSwingThread(new ThreadFunctor() {
            public void run() {
                for (Entry<String, Build> entry : jobBuildByJobNameMap.entrySet()) {
                    addFinishedBuild(rssCallback, entry.getKey(), entry.getValue());
                }

                rssContentPanel.repaint();

            }
        });
    }


    private void addFinishedBuild(RssCallback rssCallback, String jobName, Build build) {
        String buildMessage = createLinkLabel(build);
        Icon icon = setIcon(build);
        BuildResultPanel buildResultPanel = new BuildResultPanel(jobName, buildMessage, icon, build.getUrl());
        buildResultPanel.getCloseButton()
                .addActionListener(new ClosePanelAction(rssCallback, rssContentPanel, buildResultPanel));
        rssContentPanel.add(buildResultPanel);
    }


    private static String createLinkLabel(Build build) {
        return "Build #" + build.getNumber() + " " + build.getStatusValue();
    }


    private static Icon setIcon(Build build) {
        if (SUCCESS.equals(build.getStatus())) {
            return GuiUtil.loadIcon("accept.png");
        } else if (BuildStatusEnum.ABORTED.equals(build.getStatus())) {
            return GuiUtil.loadIcon("aborted.png");
        }
        return GuiUtil.loadIcon("cancel.png");
    }


    private class ClosePanelAction implements ActionListener {
        private final RssCallback callback;
        private final JPanel parentPanel;
        private final BuildResultPanel childPanel;


        private ClosePanelAction(RssCallback callback, JPanel parentPanel, BuildResultPanel childPanel) {
            this.callback = callback;
            this.parentPanel = parentPanel;
            this.childPanel = childPanel;
        }


        public void actionPerformed(ActionEvent e) {
            callback.onClearJobEntry(childPanel.getJobName());

            parentPanel.getRootPane().invalidate();
            parentPanel.remove(childPanel);
            parentPanel.getRootPane().validate();
            parentPanel.repaint();
        }
    }

    interface RssCallback {
        void onClearJobEntry(String jobName);
    }
}
