package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

class ActionUtil {

    private ActionUtil() {}

    static Project getProject(AnActionEvent event) {
            DataContext dataContext = event.getDataContext();
            return PlatformDataKeys.PROJECT.getData(dataContext);
        }
}
