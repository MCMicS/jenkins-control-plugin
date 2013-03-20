package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

public class JenkinsWindowManager {

    public static JenkinsWindowManager getInstance(Project project) {
        return ServiceManager.getService(project, JenkinsWindowManager.class);
    }

}
