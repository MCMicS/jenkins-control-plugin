package org.codinjutsu.tools.jenkins.logic;

import com.offbytwo.jenkins.helper.BuildConsoleStreamListener;
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

    void runBuild(Job job, JenkinsAppSettings configuration, Map<String, ?> parameters);

    void authenticate(JenkinsAppSettings jenkinsAppSettings, JenkinsSettings jenkinsSettings);

    String testAuthenticate(String serverUrl, String username, String password, String crumbData, JenkinsVersion version, int connectionTimoutInSeconds);

    List<Job> loadFavoriteJobs(List<JenkinsSettings.FavoriteJob> favoriteJobs);

    void stopBuild(Build build);

    @NotNull
    Job loadJob(Job job);

    @NotNull
    List<Job> loadJenkinsView(@NotNull View view);

    @NotNull
    Build loadBuild(Build build);

    List<Build> loadBuilds(Job job);

    @Deprecated(since = "0.13.17", forRemoval = true)
    String loadConsoleTextFor(Job job, BuildType buildType);

    void loadConsoleTextFor(Job job, BuildType buildType, BuildConsoleStreamListener buildConsoleStreamListener);

    void loadConsoleTextFor(Build build, BuildConsoleStreamListener buildConsoleStreamListener);

    List<TestResult> loadTestResultsFor(Job job);

    @NotNull
    List<Computer> loadComputer(JenkinsAppSettings settings);

    List<String> getGitParameterChoices(Job job, JobParameter jobParameter);
}
