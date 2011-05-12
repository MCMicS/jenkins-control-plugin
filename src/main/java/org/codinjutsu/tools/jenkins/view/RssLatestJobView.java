package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.model.Build;

import java.util.Map;

/**
 *
 */
public interface RssLatestJobView {
    void addFinishedBuild(Map<String, Build> stringBuildMap);


    void cleanRssEntries();
}
