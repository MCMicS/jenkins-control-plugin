package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.JenkinsTree;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.action.JobAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class JobClickHandler extends MouseAdapter {

    @NotNull
    private final JobAction doubleClickAction;

    public JobClickHandler(@NotNull JobAction doubleClickAction) {
        this.doubleClickAction = doubleClickAction;
    }

    @NotNull
    private static Optional<TreePath> getTreePath(MouseEvent event) {
        return Optional.ofNullable(event.getSource())
                .filter(JTree.class::isInstance).map(JTree.class::cast)
                .map(tree -> tree.getPathForLocation(event.getX(), event.getY()));
    }

    @NotNull
    private static Optional<Job> getJob(MouseEvent event) {
        return getTreePath(event).flatMap(JenkinsTree::getJob);
    }

    @Override
    public void mouseClicked(@NotNull MouseEvent event) {
        if (event.getClickCount() == 2) { // Double click
            getJob(event).ifPresent(doubleClickAction::execute);
        }
    }
}
