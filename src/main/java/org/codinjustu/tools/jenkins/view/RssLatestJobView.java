package org.codinjustu.tools.jenkins.view;

import org.codinjustu.tools.jenkins.model.Build;

import java.util.Map;

/**
 *
 */
public interface RssLatestJobView {
    void addFinishedBuild(Map<String, Build> stringBuildMap);


    void cleanRssEntries();
}
