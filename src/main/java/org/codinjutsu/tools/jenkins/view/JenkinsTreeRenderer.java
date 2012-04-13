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

import org.codinjutsu.tools.jenkins.model.Build;
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

            setToolTipText(job.findHealthDescription());
            setFont(job);
            setIcon(new CompositeIcon(job.getStateIcon(), job.getHealthIcon()));
            return this;
        }
        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
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
        return "Jenkins " + jenkins.getName();
    }
}
