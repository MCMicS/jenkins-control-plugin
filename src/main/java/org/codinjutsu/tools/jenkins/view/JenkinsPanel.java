package org.codinjutsu.tools.jenkins.view;

import javax.swing.*;
import java.awt.*;

public class JenkinsPanel extends JPanel {


    private final JenkinsBrowserPanel jenkinsBrowserPanel;
    private final RssLatestJobPanel rssLatestJobPanel;

    public JenkinsPanel() {
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        jenkinsBrowserPanel = new JenkinsBrowserPanel();
        splitPane.setTopComponent(jenkinsBrowserPanel);
        rssLatestJobPanel = new RssLatestJobPanel();
        splitPane.setBottomComponent(rssLatestJobPanel);
        splitPane.setDividerLocation(600);

        add(splitPane, BorderLayout.CENTER);
    }

    public JenkinsBrowserPanel getJenkinsBrowserPanel() {
        return jenkinsBrowserPanel;
    }

    public RssLatestJobPanel getRssLatestJobPanel() {
        return rssLatestJobPanel;
    }

}
