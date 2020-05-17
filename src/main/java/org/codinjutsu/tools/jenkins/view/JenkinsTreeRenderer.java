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

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.RowIcon;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.text.DateFormatUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.codinjutsu.tools.jenkins.model.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Optional;
import java.util.OptionalLong;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JenkinsTreeRenderer extends ColoredTreeCellRenderer {

    public static final Icon FAVORITE_ICON = AllIcons.Nodes.Favorite;

    @NotNull
    private final FavoriteJobDetector favoriteJobDetector;

    @NotNull
    private final BuildStatusRenderer buildStatusRenderer;

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded,
                                      boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        final Object userObject = node.getUserObject();
        if (userObject instanceof Jenkins) {
            Jenkins jenkins = (Jenkins) userObject;
            append(buildLabel(jenkins), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
            setToolTipText(jenkins.getServerUrl());
            setIcon(AllIcons.Webreferences.Server);
        } else if (userObject instanceof Job) {
            Job job = (Job) node.getUserObject();
            append(buildLabel(job), getAttribute(job));
            setToolTipText(job.getHealthDescription());
            if (favoriteJobDetector.isFavoriteJob(job)) {
                setIcon(new CompositeIcon(getBuildStatusColor(job), job.getHealthIcon(), FAVORITE_ICON));
            } else {
                setIcon(new CompositeIcon(getBuildStatusColor(job), job.getHealthIcon()));
            }
        } else if (userObject instanceof Build) {
            Build build = (Build) node.getUserObject();
            append(buildLabel(build), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
            setIcon(new CompositeIcon(getBuildStatusColor(build)));
        }
    }

    @NotNull
    private Icon getBuildStatusColor(Job job) {
        final JobType jobType = job.getJobType();
        if (jobType == JobType.JOB) {
            return buildStatusRenderer.renderBuildStatus(BuildStatusEnum.getStatusByColor(job.getColor()));
        }
        return jobType.getIcon();
    }

    @NotNull
    private Icon getBuildStatusColor(Build build) {
        return buildStatusRenderer.renderBuildStatus(build.getStatus());
    }

    public static SimpleTextAttributes getAttribute(Job job) {
        Build build = job.getLastBuild();
        if (build != null && (job.isInQueue() || build.isBuilding())) {
            return SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
        }
        return SimpleTextAttributes.REGULAR_ATTRIBUTES;
    }

    @NotNull
    public static String buildLabel(Build build) {
        String status = "";
        if (build.isBuilding()) {
            status = " (running)";
        }

        final String buildNumberDisplay = build.getDisplayNumber();
        final Optional<Long> duration = Optional.ofNullable(build.getDuration());
        return String.format("%s (%s) duration: %s %s", buildNumberDisplay,
                DateFormatUtil.formatDateTime(build.getTimestamp()),
                DurationFormatUtils.formatDurationHMS(duration.orElse(0L)), status);
    }

    @NotNull
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
        final String buildName = Optional.ofNullable(build.getFullDisplayName())
                .orElseGet(() -> String.format("%s %s", job.getName(), build.getDisplayNumber()));
        return String.format("%s%s", buildName, status);
    }


    public static String buildLabel(Jenkins jenkins) {
        return "Jenkins " + jenkins.getName();
    }

    private static class CompositeIcon implements Icon {

        @Delegate
        private final Icon rowIcon;

        public CompositeIcon(Icon... icons) {
            this.rowIcon = new RowIcon(icons);
        }
    }

    @FunctionalInterface
    public interface FavoriteJobDetector {

        boolean isFavoriteJob(@NotNull Job job);
    }

}
