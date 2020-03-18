package org.codinjutsu.tools.jenkins.view;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import lombok.Value;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobType;
import org.codinjutsu.tools.jenkins.view.action.LoadBuildsAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import java.util.Optional;

@Value
public class JobTreeHandler implements TreeWillExpandListener {

    private final Project project;

    @Override
    public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        getJobForNode(event).filter(job -> job.getLastBuilds().isEmpty())
                .filter(job -> job.getJobType() == JobType.JOB)
                .ifPresent(this::expandNode);
    }

    private void expandNode(@NotNull Job jobNode) {
        if (jobNode.getJobType() == JobType.JOB) {
            final AnAction action = ActionManager.getInstance().getAction(LoadBuildsAction.ACTION_ID);
            if (action instanceof LoadBuildsAction) {
                final LoadBuildsAction loadBuildsAction = (LoadBuildsAction) action;
                Optional.of(jobNode).filter(job -> job.getLastBuilds().isEmpty())
                        .ifPresent(job -> loadBuildsAction.loadBuilds(project,  job));
//            InputEvent inputEvent = ActionCommand.getInputEvent(LoadBuildsAction.ACTION_ID);
//            ActionManager.getInstance().tryToExecute(action, inputEvent, null, BrowserPanel.JENKINS_PANEL_PLACE, true);
            }
        }
    }

    @NotNull
    private Optional<Job> getJobForNode(TreeExpansionEvent event) {
        final Object node = event.getPath().getLastPathComponent();
        return Optional.ofNullable(node)
                .filter(DefaultMutableTreeNode.class::isInstance).map(DefaultMutableTreeNode.class::cast)
                .map(DefaultMutableTreeNode::getUserObject)
                .filter(Job.class::isInstance).map(Job.class::cast);
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
        // nothing to do
    }
}
