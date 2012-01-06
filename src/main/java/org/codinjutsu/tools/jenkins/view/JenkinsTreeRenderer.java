/*
 * Copyright (c) 2011 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.util.HashMap;
import java.util.Map;

class JenkinsTreeRenderer extends DefaultTreeCellRenderer {


    private static final Map<BuildStatusEnum, Icon> ICON_BY_BUILD_STATUS_MAP = new HashMap<BuildStatusEnum, Icon>();

    static {
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.SUCCESS, GuiUtil.loadIcon("blue.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.FAILURE, GuiUtil.loadIcon("red.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.NULL, GuiUtil.loadIcon("grey.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.UNSTABLE, GuiUtil.loadIcon("yellow.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.STABLE, GuiUtil.loadIcon("blue.png"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.ABORTED, GuiUtil.loadIcon("grey.png"));
    }

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

            setToolTipText(findHealthDescription(job));
            setFont(job);
            setIcon(buildJobIcon(job));
            return this;
        }
        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }

    private Icon buildJobIcon(Job job) {
        Icon jobStateIcon = findJobStateIcon(job);
        Icon healthIcon = findHealthIcon(job);
        return new CompositeIcon(jobStateIcon, healthIcon);
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


    private static Icon findJobStateIcon(Job job) {
        BuildStatusEnum[] jobStates = BuildStatusEnum.values();
        for (BuildStatusEnum jobState : jobStates) {
            String stateName = jobState.getColor();
            if (job.getColor().startsWith(stateName)) {
                return ICON_BY_BUILD_STATUS_MAP.get(jobState);
            }
        }

        return ICON_BY_BUILD_STATUS_MAP.get(BuildStatusEnum.NULL);
    }


    private static Icon findHealthIcon(Job job) {
        Job.Health health = job.getHealth();
        if (health == null) {
            return GuiUtil.loadIcon("null.png");
        }
        return GuiUtil.loadIcon(job.getHealth().getLevel() + ".png");
    }

    private static String findHealthDescription(Job job) {
        Job.Health health = job.getHealth();
        if (health == null) {
            return "";
        }
        return job.getHealth().getDescription();
    }
}
