package org.codinjutsu.tools.jenkins.view;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.ui.playback.commands.ActionCommand;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.action.RunBuildAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class JobClickHandler extends MouseAdapter {

    private static void triggerBuild(@NotNull Job job) {
        final AnAction action = ActionManager.getInstance().getAction(RunBuildAction.ACTION_ID);
        if (action instanceof RunBuildAction) {
            final InputEvent inputEvent = ActionCommand.getInputEvent(RunBuildAction.ACTION_ID);
            ActionManager.getInstance().tryToExecute(action, inputEvent, null, BrowserPanel.JENKINS_PANEL_PLACE, true);
        }
    }

    @NotNull
    private static Optional<TreePath> getTreePath(MouseEvent event) {
        return Optional.ofNullable(event.getSource())
                .filter(JTree.class::isInstance).map(JTree.class::cast)
                .map(tree -> tree.getPathForLocation(event.getX(), event.getY()));
    }

    @NotNull
    private static Optional<Job> getJob(TreePath treePath) {
        final Object node = treePath.getLastPathComponent();
        return Optional.ofNullable(node)
                .filter(DefaultMutableTreeNode.class::isInstance).map(DefaultMutableTreeNode.class::cast)
                .map(DefaultMutableTreeNode::getUserObject)
                .filter(Job.class::isInstance).map(Job.class::cast);
    }

    @NotNull
    private static Optional<Job> getJob(MouseEvent event) {
        return getTreePath(event).flatMap(JobClickHandler::getJob);
    }

    @Override
    public void mouseClicked(@NotNull MouseEvent event) {
        if (event.getClickCount() == 2) { // Double click
            getJob(event).filter(Job::isBuildable).ifPresent(JobClickHandler::triggerBuild);
        }
    }
}
