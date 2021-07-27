package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.progress.PerformInBackgroundOption;

public class JenkinsLoadingTaskOption implements PerformInBackgroundOption {

    public static final JenkinsLoadingTaskOption INSTANCE = new JenkinsLoadingTaskOption();

    public boolean shouldStartInBackground() {
        return true;
    }

    public void processSentToBackground() {

    }

}
