package org.codinjutsu.tools.jenkins;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListDecorator;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * org.codinjutsu.tools.jenkins.ChangesBarProjectComponent
 *
 * @author Yuri Novitsky
 */
public class ChangesBarProjectComponent implements ProjectComponent, ChangeListDecorator {

    private Project project;

    public ChangesBarProjectComponent(Project project) {
        this.project = project;
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "org.codinjutsu.tools.jenkins.ChangesBarProjectComponent";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }

    @Override
    public void decorateChangeList(LocalChangeList localChangeList, ColoredTreeCellRenderer coloredTreeCellRenderer, boolean b, boolean b2, boolean b3) {
        BrowserPanel browserPanel = BrowserPanel.getInstance(project);
        //browserPanel.watch();
        Map<String, Job> jobs = browserPanel.getWatched();
        if (jobs.containsKey(localChangeList.getName())) {
            Build build = jobs.get(localChangeList.getName()).getLastBuild();
            String status = build.getStatus().getStatus();
            if (build.isBuilding()) {
                status = "Running";
            }
            coloredTreeCellRenderer.append(String.format(" - build #%d: %s", build.getNumber(), status), SimpleTextAttributes.GRAYED_ATTRIBUTES);
            coloredTreeCellRenderer.repaint();
        }
    }
}
