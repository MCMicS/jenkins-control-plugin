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

package org.codinjutsu.tools.jenkins.view.util;

import com.intellij.util.ui.UIUtil;
import org.codinjutsu.tools.jenkins.logic.BuildStatusAggregator;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.view.BuildStatusRenderer;

import javax.swing.*;
import java.awt.*;

public class BuildStatusIcon extends JComponent {
    private static final int PIXEL_WIDTH = 8;

    private static final Color FOREGROUND_COLOR = new JLabel().getForeground();

    final Icon icon;

    final int numberToDisplay;
    private final int numberWith;

    public static JComponent createIcon(boolean combine, BuildStatusAggregator aggregator, BuildStatusRenderer buildStatusRenderer) {
        JPanel combined = new JPanel();

        if (aggregator.hasNoResults()) {
            return new BuildStatusIcon(buildStatusRenderer.renderBuildStatus(BuildStatusEnum.NULL), "No builds", 0);
        }

        var brokenIcon = createIcon(aggregator.getBrokenBuilds(), buildStatusRenderer, BuildStatusEnum.FAILURE, "broken");
        if (brokenIcon != null) {
            if (combine) {
                combined.add(brokenIcon);
            } else {
                return brokenIcon;
            }
        }

        var unstableIcon = createIcon(aggregator.getUnstableBuilds(), buildStatusRenderer, BuildStatusEnum.UNSTABLE, "unstable");
        if (unstableIcon != null) {
            if (combine) {
                combined.add(unstableIcon);
            } else {
                return unstableIcon;
            }
        }

        var runningIcon = createIcon(aggregator.getRunningBuilds(), buildStatusRenderer, BuildStatusEnum.RUNNING, "running");
        if (runningIcon != null) {
            if (combine) {
                combined.add(runningIcon, 0);
            } else {
                return runningIcon;
            }
        }

        var succeededIcon = createIcon(aggregator.getSucceededBuilds(), buildStatusRenderer, BuildStatusEnum.SUCCESS, "succeeded");
        if (succeededIcon != null) {
            if (combine) {
                combined.add(succeededIcon);
            } else {
                return succeededIcon;
            }
        }

        if (!combine || succeededIcon == null) {
            return new BuildStatusIcon(buildStatusRenderer.renderBuildStatus(BuildStatusEnum.SUCCESS), "No broken builds", 0);
        }

        combined.add(succeededIcon);
        return combined;
    }

    private static JComponent createIcon(int nbBrokenBuilds, BuildStatusRenderer buildStatusRenderer, BuildStatusEnum type, String label) {
        if (nbBrokenBuilds > 0) {
            return new BuildStatusIcon(buildStatusRenderer.renderBuildStatus(type), String.format("%d %s builds", nbBrokenBuilds, label), nbBrokenBuilds);
        }
        return null;
    }

    private BuildStatusIcon(Icon icon, String toolTipText, int numberToDisplay) {
        this.icon = icon;
        setToolTipText(toolTipText);
        this.numberToDisplay = numberToDisplay;
        this.numberWith = numberToDisplay == 0 ? 0 : String.valueOf(numberToDisplay).length() * PIXEL_WIDTH;
        setOpaque(false);

    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        final Insets insets = getInsets();
        return new Dimension(
                icon.getIconWidth() + insets.left + insets.right + numberWith,
                icon.getIconHeight() + insets.top + insets.bottom
        );
    }

    @Override
    protected void paintComponent(Graphics g) {

        g.setColor(UIUtil.getPanelBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        final Dimension size = getSize();
        int x = (size.width - icon.getIconWidth() - numberWith) / 2;
        int y = (size.height - icon.getIconHeight()) / 2;
        paintIcon(g, icon, x, y);

        if (numberToDisplay > 0) {
            Font originalFont = g.getFont();
            Color originalColor = g.getColor();
            g.setFont(calcFont());
            y += icon.getIconHeight() - g.getFontMetrics().getDescent();
            x += icon.getIconWidth();

            g.setColor(FOREGROUND_COLOR);
            g.drawString(String.valueOf(numberToDisplay), x, y);

            g.setFont(originalFont);
            g.setColor(originalColor);
        }
    }


    private void paintIcon(Graphics g, Icon icon, int x, int y) {
        icon.paintIcon(this, g, x, y);
    }


    private Font calcFont() {
        return getFont().deriveFont(Font.BOLD).deriveFont((float) icon.getIconHeight() * 3 / 5);
    }
}
