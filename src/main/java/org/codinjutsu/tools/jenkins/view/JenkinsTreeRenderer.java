/*
 * Copyright (c) 2012 David Boissier
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

import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.List;

class JenkinsTreeRenderer extends DefaultTreeCellRenderer {

    private static final Icon FAVORITE_ICON = GuiUtil.loadIcon("star_tn.png");
    private static final Icon SERVER_ICON = GuiUtil.loadIcon("server_wrench.png");
    private static final Icon SERVER_ERROR_ICON = GuiUtil.loadIcon("server_error.png");

    private final List<JenkinsConfiguration.FavoriteJob> favoriteJobs;

    public JenkinsTreeRenderer(List<JenkinsConfiguration.FavoriteJob> favoriteJobs) {
        this.favoriteJobs = favoriteJobs;
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
                setIcon(SERVER_ICON);
            } else {
                setIcon(SERVER_ERROR_ICON);
            }
            setToolTipText(jenkins.getServerUrl());
            setFont(getFont().deriveFont(Font.ITALIC));

            return this;
        } else if (userObject instanceof Job) {
            Job job = (Job) node.getUserObject();

            String jobLabel = buildLabel(job);

            super.getTreeCellRendererComponent(tree, jobLabel, sel,
                    expanded, leaf, row,
                    hasFocus);

            setFont(job);
            setToolTipText(job.findHealthDescription());
            if (isFavoriteJob(job)) {
                setIcon(new CompositeIcon(job.getStateIcon(), job.getHealthIcon(), FAVORITE_ICON));
                this.repaint();
            } else {
                setIcon(new CompositeIcon(job.getStateIcon(), job.getHealthIcon()));
            }
            updateUI();
            return this;
        }
        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }

    private boolean isFavoriteJob(Job job) {
        for (JenkinsConfiguration.FavoriteJob favoriteJob : favoriteJobs) {
            if (favoriteJob.name.equals(job.getName())) {
                return true;
            }
        }
        return false;
    }

    private void setFont(Job job) {
        Font font = getFont().deriveFont(Font.PLAIN);
        Build build = job.getLastBuild();
        if (build != null) {
            if (job.isInQueue() || build.isBuilding()) {
                font = font.deriveFont(Font.BOLD);
            }
        }

        setFont(font);
    }


    private static String buildLabel(Job job) {

        Build build = job.getLastBuild();
        if (build == null) {
            return job.getName();
        }
        String status = "";
        if (job.isInQueue()) {
            status = " (in queue)";
        } else if (build.isBuilding()) {
            status = " (running)";
        }
        return String.format("%s #%s%s", job.getName(), build.getNumber(), status);
    }


    private static String buildLabel(Jenkins jenkins) {
        return "Jenkins " + jenkins.getName();
    }
}
