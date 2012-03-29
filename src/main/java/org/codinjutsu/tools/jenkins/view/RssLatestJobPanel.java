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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.SUCCESS;

public class RssLatestJobPanel extends JPanel{

    private static final Icon OK_ICON = GuiUtil.loadIcon("accept.png");
    private static final Icon ABORT_ICON = GuiUtil.loadIcon("aborted.png");
    private static final Icon CANCEL_ICON = GuiUtil.loadIcon("cancel.png");

    private JPanel rssContentPanel;
    private JPanel rootPanel;
    private JPanel rssActionPanel;
    private final boolean reverseMode;

    public RssLatestJobPanel(boolean reverseMode) {
        this.reverseMode = reverseMode;
        rssContentPanel.setLayout(new BoxLayout(rssContentPanel, BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
    }


    public void cleanRssEntries() {
        GuiUtil.runInSwingThread(new Runnable() {
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
         if( reverseMode) {

         }
        
        
        final RssCallback rssCallback = new RssCallback() {

            public void onClearJobEntry(String jobName) {
                jobBuildByJobNameMap.remove(jobName);
            }
        };

        GuiUtil.runInSwingThread(new Runnable() {
            public void run() {
                for (Entry<String, Build> entry : jobBuildByJobNameMap.entrySet()) {
                    addFinishedBuild(rssCallback, entry.getKey(), entry.getValue());
                }

                rssContentPanel.repaint();

            }
        });
    }


    private void addFinishedBuild(RssCallback rssCallback, String jobName, Build build) {
        String buildMessage = createLinkLabel(jobName, build);
        Icon icon = setIcon(build);
        BuildResultPanel buildResultPanel = new BuildResultPanel(jobName, buildMessage, icon, build.getUrl());
        buildResultPanel.getCloseButton()
                .addActionListener(new ClosePanelAction(rssCallback, rssContentPanel, buildResultPanel));
        rssContentPanel.add(buildResultPanel);
    }


    private static String createLinkLabel(String jobName, Build build) {
        return jobName + " #" + build.getNumber() + " " + build.getStatusValue();
    }


    private static Icon setIcon(Build build) {
        if (SUCCESS.equals(build.getStatus())) {
            return OK_ICON;
        } else if (BuildStatusEnum.ABORTED.equals(build.getStatus())) {
            return ABORT_ICON;
        }
        return CANCEL_ICON;
    }

    public JPanel getRssActionPanel() {
        return rssActionPanel;
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
