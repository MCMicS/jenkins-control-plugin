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

import com.intellij.openapi.ui.Splitter;

import javax.swing.*;
import java.awt.*;

public class JenkinsPanel extends JPanel {

    public static JenkinsPanel onePanel(BrowserPanel browserPanel, RssLatestBuildPanel rssLatestJobPanel) {
        return new JenkinsPanel(browserPanel, rssLatestJobPanel);
    }

    private JenkinsPanel(BrowserPanel browserPanel, RssLatestBuildPanel rssLatestJobPanel) {
        setLayout(new BorderLayout());

        Splitter splitter = new Splitter(true);
        splitter.setFirstComponent(browserPanel);
        splitter.setSecondComponent(rssLatestJobPanel);
        splitter.setShowDividerControls(true);
        splitter.setProportion(0.60f);

        add(splitter, BorderLayout.CENTER);
    }
}
