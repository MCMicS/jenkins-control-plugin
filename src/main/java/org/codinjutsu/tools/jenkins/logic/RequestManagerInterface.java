package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.vfs.VirtualFile;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;

import java.util.List;
import java.util.Map;

/**
 * Created by marcin on 07.10.15.
 */
public interface RequestManagerInterface {
    Jenkins loadJenkinsWorkspace(JenkinsAppSettings configuration);

    Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsAppSettings configuration);

    void runBuild(Job job, JenkinsAppSettings configuration, Map<String, VirtualFile> files);

    void runBuild(Job job, JenkinsAppSettings configuration);

    void runParameterizedBuild(Job job, JenkinsAppSettings configuration, Map<String, String> paramValueMap);

    void authenticate(JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings);

    void authenticate(String serverUrl, String username, String password, String crumbData);

    List<Job> loadFavoriteJobs(List<JenkinsSettings.FavoriteJob> favoriteJobs);

    boolean stopBuild(Build build);

    Job loadJob(Job job);

    List<Job>loadJenkinsView (View view);

    Build loadBuild(Build build);
}
