package org.codinjutsu.tools.jenkins.util;

import com.intellij.openapi.project.Project;
import lombok.experimental.UtilityClass;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@UtilityClass
public class MockUtil {

    public static Project mockProject() {
        return mock(Project.class);
    }

    public static Project mockProject(Object... servicesToReturn) {
        final Project project = mockProject();
        for (Object service : servicesToReturn) {
            final var serviceClass = service.getClass();
            doReturn(service).when(project).getService(serviceClass);
        }
        return project;
    }

}
