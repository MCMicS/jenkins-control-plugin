package org.codinjutsu.tools.jenkins;

import java.text.MessageFormat;
import java.util.Map;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.logic.RunBuildWithPatch;
import org.codinjutsu.tools.jenkins.model.Job;

public class TraceableBuildJobFactory {
    private static int RETRY_LIMIT = 10;

    public static TraceableBuildJob newBuildJob(Job job, JenkinsAppSettings configuration, Map<String, ?> paramValueMap,
            RequestManager requestManager) {
        final int numBuildRetries = configuration.getNumBuildRetries();
        ensureRetryLimit(numBuildRetries);
//        final Runnable runBuild = () -> ApplicationManager.getApplication().invokeLater(
//                () -> requestManager.runParameterizedBuild(job, configuration, paramValueMap),
//                ModalityState.NON_MODAL);
        final Runnable runBuild = () -> requestManager.runParameterizedBuild(job, configuration, paramValueMap);
        return new TraceableBuildJob(job, runBuild, numBuildRetries);
    }

    private static void ensureRetryLimit(int numBuildRetries) {
        if (numBuildRetries > RETRY_LIMIT) {
            throw new IllegalArgumentException(MessageFormat.format("can't retry more than {0} times", RETRY_LIMIT));
        }
    }
}
