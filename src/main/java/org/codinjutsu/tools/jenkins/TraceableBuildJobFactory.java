package org.codinjutsu.tools.jenkins;

import org.codinjutsu.tools.jenkins.logic.RequestManagerInterface;
import org.codinjutsu.tools.jenkins.model.Job;

import java.text.MessageFormat;
import java.util.Map;

public class TraceableBuildJobFactory {
    private static int RETRY_LIMIT = 10;

    public static TraceableBuildJob newBuildJob(Job job, JenkinsAppSettings configuration, Map<String, ?> paramValueMap,
                                                RequestManagerInterface requestManager) {
        final int numBuildRetries = configuration.getNumBuildRetries();
        ensureRetryLimit(numBuildRetries);
        final Runnable runBuild = () -> requestManager.runBuild(job, configuration, paramValueMap);
        return new TraceableBuildJob(job, runBuild, numBuildRetries);
    }

    private static void ensureRetryLimit(int numBuildRetries) {
        if (numBuildRetries > RETRY_LIMIT) {
            throw new IllegalArgumentException(MessageFormat.format("can't retry more than {0} times", RETRY_LIMIT));
        }
    }
}
