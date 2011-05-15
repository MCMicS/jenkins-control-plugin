package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

class JenkinsTreeRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        Object userObject = node.getUserObject();
        if (userObject instanceof Jenkins) {
            Jenkins jenkins = (Jenkins) userObject;
            super.getTreeCellRendererComponent(tree, buildLabel(jenkins), sel,
                    expanded, leaf, row,
                    hasFocus);
            if (!jenkins.getJobs().isEmpty()) {
                setIcon(GuiUtil.loadIcon("server_wrench.png"));
            } else {
                setIcon(GuiUtil.loadIcon("server_error.png"));
            }
            setFont(getFont().deriveFont(Font.ITALIC));

            return this;
        } else if (userObject instanceof Job) {
            Job job = (Job) node.getUserObject();

            String jobLabel = buildLabel(job);
            
            super.getTreeCellRendererComponent(tree, jobLabel, sel,
                    expanded, leaf, row,
                    hasFocus);

            setToolTipText(jobLabel);
            setFont(job);
            setIcon(findRightJobStateIcon(job));
            return this;
        }
        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }


    private void setFont(Job job) {
        Font font = getFont();
        Build build = job.getLastBuild();
        setFont(font.deriveFont(Font.PLAIN));
        if (build != null) {
            if (job.isInQueue() || build.isBuilding()) {
                setFont(font.deriveFont(Font.BOLD));
            }
        }
    }


    private static String buildLabel(Job job) {

        Build build = job.getLastBuild();
        StringBuilder stringBuilder = new StringBuilder(job.getName());
        if (build != null) {
            stringBuilder.append(" #").append(build.getNumber());
            if (job.isInQueue()) {
                stringBuilder.append(" (in queue)");
            } else if (build.isBuilding()) {
                stringBuilder.append(" (running)");
            }
        }

        return stringBuilder.toString();
    }


    private static String buildLabel(Jenkins jenkins) {

        return new StringBuilder("Jenkins ")
                .append(jenkins.getName())
                .toString();
    }


    private static Icon findRightJobStateIcon(Job job) {
        BuildStatusEnum[] jobStates = BuildStatusEnum.values();
        for (BuildStatusEnum jobState : jobStates) {
            String stateName = jobState.getColor();
            if (job.getColor().startsWith(stateName)) {
                return jobState.getIcon();
            }
        }

        return BuildStatusEnum.NULL.getIcon();
    }
}
