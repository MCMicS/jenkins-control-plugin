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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.codinjutsu.tools.jenkins.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Optional;
import java.util.function.Function;

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
        getJenkinsTreeNode(value).ifPresent(jenkinsTreeNode -> render(jenkinsTreeNode, getNode(value).map(DefaultMutableTreeNode::getParent)));
    }

    private void render(@NotNull JenkinsTreeNode treeNode, @NotNull Optional<TreeNode> parent) {
        treeNode.render(new JenkinsTreeNodeVisitor() {
            @Override
            public void visit(JenkinsTreeNode.RootNode jenkinsServer) {
                JenkinsTreeRenderer.this.render(jenkinsServer);
            }

            @Override
            public void visit(JenkinsTreeNode.BuildNode build) {
                JenkinsTreeRenderer.this.render(build);
            }

            @Override
            public void visit(JenkinsTreeNode.JobNode job) {
                JenkinsTreeRenderer.this.render(job, parent);
            }

            @Override
            public void visit(JenkinsTreeNode.BuildParameterNode buildParameterNode) {
                JenkinsTreeRenderer.this.render(buildParameterNode);
            }
        });
    }

    @NotNull
    private Optional<DefaultMutableTreeNode> getNode(@Nullable Object value) {
        return Optional.ofNullable(value).filter(DefaultMutableTreeNode.class::isInstance)
                .map(DefaultMutableTreeNode.class::cast);
    }

    @NotNull
    private Optional<Object> getUserObject(@Nullable Object value) {
        return getNode(value).map(DefaultMutableTreeNode::getUserObject);
    }

    @NotNull
    private Optional<JenkinsTreeNode> getJenkinsTreeNode(@Nullable Object value) {
        return getUserObject(value).filter(JenkinsTreeNode.class::isInstance).map(JenkinsTreeNode.class::cast);
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
    public static String buildLabel(Job job, Optional<JenkinsTreeNode> parentJenkinsTreeNode) {
        final Function<Job, String> jobNameRenderer;
        final Function<Build, String> buildNameRenderer;
        if (parentJenkinsTreeNode.filter(JenkinsTreeNode.JobNode.class::isInstance).isPresent()) {
            jobNameRenderer = Job::preferDisplayName;
            buildNameRenderer = build -> StringUtils.EMPTY;
        } else {
            jobNameRenderer = Job::getNameToRenderSingleJob;
            buildNameRenderer = Build::getFullDisplayName;
        }
        return buildLabel(job, jobNameRenderer, buildNameRenderer);
    }

    @NotNull
    public static String buildLabel(Job job, Function<Job, String> jobName, Function<Build, String> buildName) {
        Build build = job.getLastBuild();
        if (build == null) {
            return jobName.apply(job);
        }
        String status = "";
        if (job.isInQueue()) {
            status = "(in queue)";
        } else if (build.isBuilding()) {
            status = "(running)";
        }
        final String renderedValue = Optional.ofNullable(buildName.apply(build))
                .filter(StringUtils::isNotEmpty)
                .orElseGet(() -> String.format("%s %s", jobName.apply(job), build.getDisplayNumber()));
        return String.format("%s %s", renderedValue, status);
    }


    public static String buildLabel(Jenkins jenkins) {
        final var description = jenkins.getName();
        final var label = new StringBuilder("Jenkins");
        if (StringUtils.isNotEmpty(description)) {
            label.append(' ');
            label.append(description);
        }
        return label.toString();
    }

    private void render(JenkinsTreeNode.RootNode jenkinsServer) {
        final Jenkins jenkins = jenkinsServer.getJenkins();
        append(buildLabel(jenkins), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
        setToolTipText(jenkins.getServerUrl());
        setIcon(AllIcons.Webreferences.Server);
    }

    private void render(JenkinsTreeNode.BuildNode build) {
        append(buildLabel(build.getBuild()), SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
        setIcon(new CompositeIcon(getBuildStatusColor(build.getBuild())));
    }

    private void render(JenkinsTreeNode.JobNode jobNode, @NotNull Optional<TreeNode> parent) {
        final Job job = jobNode.getJob();
        append(buildLabel(job, parent.flatMap(this::getJenkinsTreeNode)), getAttribute(job));
        setToolTipText(job.getHealthDescription());
        if (favoriteJobDetector.isFavoriteJob(job)) {
            setIcon(new CompositeIcon(getBuildStatusColor(job), job.getHealthIcon(), FAVORITE_ICON));
        } else {
            setIcon(new CompositeIcon(getBuildStatusColor(job), job.getHealthIcon()));
        }
    }

    private void render(JenkinsTreeNode.BuildParameterNode buildParameterNode) {
        final String parameter;
        if (buildParameterNode.hasValue()) {
            parameter = String.format("%s: %s", buildParameterNode.getBuildParameter().getName(),
                    buildParameterNode.getBuildParameter().getValue());
        } else {
            parameter = buildParameterNode.getBuildParameter().getName();
        }
        append(parameter, SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
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
