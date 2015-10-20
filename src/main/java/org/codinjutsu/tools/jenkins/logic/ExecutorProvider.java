package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ExecutorProvider {

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public ExecutorProvider() {
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    }

    public ScheduledThreadPoolExecutor getExecutor() {
        return scheduledThreadPoolExecutor;
    }

    public static ExecutorProvider getInstance(Project project) {
        return ServiceManager.getService(project, ExecutorProvider.class);
    }
}
