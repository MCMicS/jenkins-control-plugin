/*
 * Copyright (c) 2013 David Boissier
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

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

class JenkinsTreeRenderer extends ColoredTreeCellRenderer {

    private static final Icon FAVORITE_ICON = GuiUtil.loadIcon("star_tn.png");
    private static final Icon SERVER_ICON = GuiUtil.loadIcon("server_wrench.png");

    private final List<JenkinsSettings.FavoriteJob> favoriteJobs;

    public JenkinsTreeRenderer(List<JenkinsSettings.FavoriteJob> favoriteJobs) {
        this.favoriteJobs = favoriteJobs;
    }

    @Override
    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        Object userObject = node.getUserObject();
        if (userObject instanceof Jenkins) {
            Jenkins jenkins = (Jenkins) userObject;
            append(buildLabel(jenkins), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
            setToolTipText(jenkins.getServerUrl());
            setIcon(SERVER_ICON);

        } else if (userObject instanceof Job) {
            Job job = (Job) node.getUserObject();

            append(buildLabel(job), getAttribute(job));

            setToolTipText(job.findHealthDescription());
            if (isFavoriteJob(job)) {
                setIcon(new CompositeIcon(job.getStateIcon(), job.getHealthIcon(), FAVORITE_ICON));
            } else {
                setIcon(new CompositeIcon(job.getStateIcon(), job.getHealthIcon()));
            }
        }
    }

    private boolean isFavoriteJob(Job job) {
        for (JenkinsSettings.FavoriteJob favoriteJob : favoriteJobs) {
            if (favoriteJob.name.equals(job.getName())) {
                return true;
            }
        }
        return false;
    }

    private SimpleTextAttributes getAttribute(Job job) {
        Build build = job.getLastBuild();
        if (build != null) {
            if (job.isInQueue() || build.isBuilding()) {
                return SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
            }
        }

        return SimpleTextAttributes.REGULAR_ATTRIBUTES;
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
