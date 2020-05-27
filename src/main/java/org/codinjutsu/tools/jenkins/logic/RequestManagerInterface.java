package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.vfs.VirtualFile;
import com.offbytwo.jenkins.model.TestResult;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.security.JenkinsVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface RequestManagerInterface {
    Jenkins loadJenkinsWorkspace(JenkinsAppSettings configuration);

    Map<String, Build> loadJenkinsRssLatestBuilds(JenkinsAppSettings configuration);

    void runBuild(Job job, JenkinsAppSettings configuration, Map<String, VirtualFile> files);

    void runBuild(Job job, JenkinsAppSettings configuration);

    void runParameterizedBuild(Job job, JenkinsAppSettings configuration, Map<String, String> paramValueMap);

    void authenticate(JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings);

    void authenticate(String serverUrl, String username, String password, String crumbData, JenkinsVersion version);

    List<Job> loadFavoriteJobs(List<JenkinsSettings.FavoriteJob> favoriteJobs);

    void stopBuild(Build build);

    @NotNull
    Job loadJob(Job job);

    List<Job>loadJenkinsView (View view);

    @NotNull
    Build loadBuild(Build build);

    List<Build> loadBuilds(Job job);

    String loadConsoleTextFor(Job job);

    List<TestResult> loadTestResultsFor(Job job);

    @NotNull
    List<Computer> loadComputer(JenkinsAppSettings settings);
}
