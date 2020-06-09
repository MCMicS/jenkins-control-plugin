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

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

public class BuildStatusIcon extends JComponent {


    private static final int PIXEL_WIDTH = 8;

    private static final Color FOREGROUND_COLOR = new JLabel().getForeground();

    final Icon icon;
    final String toolTipText;

    final int numberToDisplay;
    private final int numberWith;

    public static JComponent createIcon(boolean combine, BuildStatusAggregator aggregator, BuildStatusRenderer buildStatusRenderer) {
        JPanel combined = new JPanel();

        if (aggregator.hasNoResults()) {
            return new BuildStatusIcon(buildStatusRenderer.renderBuildStatus(BuildStatusEnum.NULL),
                    "No builds", 0);
        }

        int nbBrokenBuilds = aggregator.getBrokenBuilds();
        if (nbBrokenBuilds > 0) {
            BuildStatusIcon brokenIcon = new BuildStatusIcon(buildStatusRenderer.renderBuildStatus(BuildStatusEnum.FAILURE),
                    String.format("%d broken builds", nbBrokenBuilds), nbBrokenBuilds);
            if (!combine) {
                return brokenIcon;
            }
            combined.add(brokenIcon);
        }

        int nbUnstableBuilds = aggregator.getUnstableBuilds();
        if (nbUnstableBuilds > 0) {
            BuildStatusIcon unstableIcon = new BuildStatusIcon(buildStatusRenderer.renderBuildStatus(BuildStatusEnum.UNSTABLE),
                    String.format("%d unstable builds", nbUnstableBuilds), nbUnstableBuilds);
            if (!combine) {
                return unstableIcon;
            }
            combined.add(unstableIcon);
        }

        String succeededTooltip = combine ? String.format("%s succeeded builds", aggregator.getSucceededBuilds()) : "No broken builds";
        int succeededNumber = combine ? aggregator.getSucceededBuilds() : 0;
        BuildStatusIcon successIcon = new BuildStatusIcon(buildStatusRenderer.renderBuildStatus(BuildStatusEnum.SUCCESS), succeededTooltip, succeededNumber);
        if (!combine) {
            return successIcon;
        }

        combined.add(successIcon);
        return combined;
    }

    private BuildStatusIcon(Icon icon, String toolTipText, int numberToDisplay) {
        this.icon = icon;
        this.toolTipText = toolTipText;
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
        setToolTipText(toolTipText);

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
