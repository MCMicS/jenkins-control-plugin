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

import com.intellij.ide.BrowserUtil;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import static org.codinjutsu.tools.jenkins.util.DateUtil.LOG_DATE_IN_HOUR_FORMAT;
import static org.codinjutsu.tools.jenkins.util.DateUtil.format;

public class RssLatestBuildPanel extends JPanel {
    private JPanel rssActionPanel;

    private JPanel rootPanel;
    private JTextPane rssTextPane;
    private HTMLEditorKit htmlEditorKit;
    private HTMLDocument htmlDocument;

    public RssLatestBuildPanel() {

        initRssPane();

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
    }

    private void initRssPane() {
        rssTextPane.setName("rssContent");
        htmlEditorKit = new HTMLEditorKit();
        htmlDocument = new HTMLDocument();

        rssTextPane.setEditable(false);
        rssTextPane.setBackground(Color.WHITE);
        rssTextPane.setEditorKit(htmlEditorKit);
        htmlEditorKit.install(rssTextPane);
        rssTextPane.setDocument(htmlDocument);
        rssTextPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                HyperlinkEvent.EventType eventType = hyperlinkEvent.getEventType();
                if (HyperlinkEvent.EventType.ACTIVATED.equals(eventType)) {
                    BrowserUtil.launchBrowser(hyperlinkEvent.getURL().toString());
                }
            }
        });
    }

    public void cleanRssEntries() {
        htmlDocument = new HTMLDocument();
        rssTextPane.setDocument(htmlDocument);
    }

    public void addFinishedBuild(final Map<String, Build> buildByJobNameMap) {
        final ArrayList<Build> buildToSortByDateDescending = new ArrayList<Build>(buildByJobNameMap.values());

        Collections.sort(buildToSortByDateDescending, new Comparator<Build>() {
            @Override
            public int compare(Build firstBuild, Build secondBuild) {
                return firstBuild.getBuildDate().compareTo(secondBuild.getBuildDate());
            }
        });

        GuiUtil.runInSwingThread(new Runnable() {
            @Override
            public void run() {
                for (Build build : buildToSortByDateDescending) {
                    appendHtmlText(buildMessage(build));
                }
                rssTextPane.setCaretPosition(htmlDocument.getLength());
            }
        });
    }

    private void appendHtmlText(String textToAppend) {
        try {
            htmlEditorKit.insertHTML(htmlDocument, htmlDocument.getLength(), textToAppend, 0, 0, null);
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    private static String buildMessage(Build build) {
        String time = format(build.getBuildDate(), LOG_DATE_IN_HOUR_FORMAT);
        BuildStatusEnum buildStatus = build.getStatus();
        String statusColor = buildStatus.getColor().toLowerCase();
        String buildMessage = build.getMessage();
        String coloredLinkText = String.format("<font color='%s'>%s</font>", statusColor, buildMessage);

        if (buildStatus != BuildStatusEnum.SUCCESS && buildStatus != BuildStatusEnum.STABLE) {
            return String.format("%s <a href='%s'>%s</a><br/>", time, build.getUrl(), coloredLinkText);
        }
        return String.format("%s %s<br/>", time, coloredLinkText);

    }

    public JPanel getRssActionPanel() {
        return rssActionPanel;
    }
}
