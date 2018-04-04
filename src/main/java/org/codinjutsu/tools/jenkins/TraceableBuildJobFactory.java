package org.codinjutsu.tools.jenkins;

import java.text.MessageFormat;
import java.util.Map;

import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;

public class TraceableBuildJobFactory {
    private static int RETRY_LIMIT = 10;

    public static TraceableBuildJob newBuildJob(Job job, JenkinsAppSettings configuration, Map<String, String> paramValueMap,
            RequestManager requestManager) {
        int numBuildRetries = configuration.getNumBuildRetries();
        ensureRetryLimit(numBuildRetries);
        Runnable runBuild = () -> requestManager.runParameterizedBuild(job, configuration, paramValueMap);
        return new TraceableBuildJob(job, runBuild, numBuildRetries);
    }

    private static void ensureRetryLimit(int numBuildRetries) {
        if (numBuildRetries > RETRY_LIMIT) {
            throw new IllegalArgumentException(MessageFormat.format("can't retry more than {0} times", RETRY_LIMIT));
        }
    }
}
