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

import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static org.codinjutsu.tools.jenkins.util.DateUtil.LOG_DATE_FORMAT;
import static org.codinjutsu.tools.jenkins.util.DateUtil.format;

public class RssLatestBuildPanel extends JPanel {
    private JPanel rssActionPanel;
    private JTextArea rssLatestBuildLogTextArea;
    private JPanel rootPanel;

    public RssLatestBuildPanel() {

        rssLatestBuildLogTextArea.setName("rssContent");
        rssLatestBuildLogTextArea.setEditable(false);

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
    }

    public void cleanRssEntries() {
        rssLatestBuildLogTextArea.setText("");
    }

    public void addFinishedBuild(final Map<String, Build> buildByJobNameMap) {
        ArrayList<Build> buildToSortByDateDescending = new ArrayList<Build>(buildByJobNameMap.values());

        Collections.sort(buildToSortByDateDescending, new Comparator<Build>() {
            @Override
            public int compare(Build firstBuild, Build secondBuild) {
                return firstBuild.getBuildDate().compareTo(secondBuild.getBuildDate());
            }
        });

        StringBuilder stringBuilder = new StringBuilder();
        for (Build build : buildToSortByDateDescending) {
            stringBuilder.append(format(build.getBuildDate(), LOG_DATE_FORMAT)).append(" ").append(build.getMessage() + "\n");
        }

        rssLatestBuildLogTextArea.append(stringBuilder.toString());
    }

    public JPanel getRssActionPanel() {
        return rssActionPanel;
    }
}
