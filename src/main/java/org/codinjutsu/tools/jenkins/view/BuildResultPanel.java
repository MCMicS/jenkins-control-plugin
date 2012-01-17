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

import com.intellij.ide.BrowserUtil;
import com.intellij.ui.HyperlinkLabel;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public class BuildResultPanel extends JPanel {
    private JButton closeButton;
    private JPanel rootPanel;
    private JPanel buildStatusPanel;
    private final String jobName;


    public BuildResultPanel(String jobName, String buildMessage, Icon icon, final String buildUrl) {
        this.jobName = jobName;
        buildStatusPanel.setLayout(new BorderLayout());

        HyperlinkLabel buildStatusLabel = new HyperlinkLabel(buildMessage);
        buildStatusLabel.setIcon(icon);
        buildStatusLabel.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(final HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    BrowserUtil.launchBrowser(buildUrl);
                }
            }
        });

        buildStatusPanel.add(buildStatusLabel, BorderLayout.CENTER);

        closeButton.setIcon(GuiUtil.loadIcon("cross.png"));

        rootPanel.setBorder(BorderFactory.createTitledBorder(jobName));

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
    }


    public JButton getCloseButton() {
        return closeButton;
    }

    public String getJobName() {
        return jobName;
    }
}
