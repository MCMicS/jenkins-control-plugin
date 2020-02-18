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
import com.intellij.ui.RowIcon;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.text.DateFormatUtil;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

public class JenkinsTreeRenderer extends ColoredTreeCellRenderer {

    public static final Icon FAVORITE_ICON = GuiUtil.loadIcon("star_tn.png");
    public static final Icon SERVER_ICON = GuiUtil.loadIcon("server_wrench.png");

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

            setToolTipText(job.getHealthDescription());
            if (isFavoriteJob(job)) {
                setIcon(new CompositeIcon(job.getStateIcon(), job.getHealthIcon(), FAVORITE_ICON));
            } else {
                setIcon(new CompositeIcon(job.getStateIcon(), job.getHealthIcon()));
            }
        } else if (userObject instanceof Build) {
            Build build = (Build) node.getUserObject();
            append(buildLabel(build), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
            setIcon(new CompositeIcon(build.getStateIcon()));
        }
    }

    boolean isFavoriteJob(Job job) {
        for (JenkinsSettings.FavoriteJob favoriteJob : favoriteJobs) {
            if (favoriteJob.name.equals(job.getName())) {
                return true;
            }
        }
        return false;
    }

    public static SimpleTextAttributes getAttribute(Job job) {
        Build build = job.getLastBuild();
        if (build != null) {
            if (job.isInQueue() || build.isBuilding()) {
                return SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
            }
        }

        return SimpleTextAttributes.REGULAR_ATTRIBUTES;
    }

    public static String buildLabel(Build build) {
        String status = "";
        if (build.isBuilding()) {
            status = " (running)";
        }
        return String.format("#%d (%s) duration: %s %s", build.getNumber(), DateFormatUtil.formatDateTime(build.getTimestamp()), DurationFormatUtils.formatDurationHMS(build.getDuration()), status);
    }


    public static String buildLabel(Job job) {

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


    public static String buildLabel(Jenkins jenkins) {
        return "Jenkins " + jenkins.getName();
    }

    private static class CompositeIcon extends RowIcon {

        public CompositeIcon(Icon... icons) {
            super(icons.length);
            for (int i = 0; i < icons.length; i++) {
                Icon icon = icons[i];
                setIcon(icon, i);
            }
        }
    }
}
