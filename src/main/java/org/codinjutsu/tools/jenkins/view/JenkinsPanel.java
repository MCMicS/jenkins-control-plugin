package org.codinjutsu.tools.jenkins.view;

import javax.swing.*;
import java.awt.*;

public class JenkinsPanel extends JPanel {


    private JenkinsBrowserPanel browserPanel;
    private RssLatestJobPanel rssLatestJobPanel;

    public static JenkinsPanel onePanel(JenkinsBrowserPanel jenkinsBrowserPanel, RssLatestJobPanel rssLatestJobPanel) {
        return new JenkinsPanel(jenkinsBrowserPanel, rssLatestJobPanel);
    }

    public static JenkinsPanel browserOnly(JenkinsBrowserPanel jenkinsBrowserPanel) {
        return new JenkinsPanel(jenkinsBrowserPanel);
    }

    private JenkinsPanel(JenkinsBrowserPanel jenkinsBrowserPanel) {
        setLayout(new BorderLayout());
        add(jenkinsBrowserPanel, BorderLayout.CENTER);
    }

    public JenkinsPanel(JenkinsBrowserPanel jenkinsBrowserPanel, RssLatestJobPanel rssLatestJobPanel) {
        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(jenkinsBrowserPanel);
        splitPane.setBottomComponent(rssLatestJobPanel);
        splitPane.setDividerLocation(600);

        add(splitPane, BorderLayout.CENTER);
    }

    public JenkinsBrowserPanel getBrowserPanel() {
        return browserPanel;
    }

    public RssLatestJobPanel getRssLatestJobPanel() {
        return rssLatestJobPanel;
    }
}
