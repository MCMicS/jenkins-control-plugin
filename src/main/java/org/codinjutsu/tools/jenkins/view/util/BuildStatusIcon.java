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

package org.codinjutsu.tools.jenkins.view.util;

import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.ui.UIUtil;
import org.codinjutsu.tools.jenkins.logic.BuildStatusAggregator;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

public class BuildStatusIcon extends JComponent {


    private final Icon icon;

    private final String toolTipText;

    public static JComponent createIcon(BuildStatusAggregator aggregator, MouseAdapter mouseAdapter) {
        if (aggregator.hasNoResults()) {
            return new BuildStatusIcon(GuiUtil.loadIcon("grey.png"), "No builds", mouseAdapter);
        }

        int nbBrokenBuilds = aggregator.getNbBrokenBuilds();
        if (nbBrokenBuilds > 0) {
            return new BuildStatusIcon(GuiUtil.loadIcon("red.png"), String.format("%d broken builds", nbBrokenBuilds), mouseAdapter);
        }

        int nbUnstableBuilds = aggregator.getNbUnstableBuilds();
        if (nbUnstableBuilds > 0) {
            return new BuildStatusIcon(GuiUtil.loadIcon("yellow.png"), String.format("%d unstable builds", nbUnstableBuilds), mouseAdapter);
        }

        return new BuildStatusIcon(GuiUtil.loadIcon("blue.png"), "No broken builds", mouseAdapter);
    }

    private BuildStatusIcon(Icon icon, String toolTipText, MouseListener mouseListener) {
        this.icon = icon;
        this.toolTipText = toolTipText;
        addMouseListener(mouseListener);
        UIUtil.removeQuaquaVisualMarginsIn(this);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(StatusBarWidget.WidgetBorder.INSTANCE);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        final Insets insets = getInsets();
        return new Dimension(
                icon.getIconWidth() + insets.left + insets.right,
                icon.getIconHeight() + insets.top + insets.bottom
        );
    }

    protected void paintComponent(Graphics g) {

        g.setColor(UIUtil.getPanelBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        final Dimension size = getSize();
        int x = (size.width - icon.getIconWidth()) / 2;
        int y = (size.height - icon.getIconHeight()) / 2;
        paintIcon(g, icon, x, y);
        setToolTipText(toolTipText);
    }


    protected void paintIcon(Graphics g, Icon icon, int x, int y) {
        icon.paintIcon(this, g, x, y);
    }
}
