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

import com.intellij.util.ui.UIUtil;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public abstract class BuildStatusIcon extends JComponent {


    protected abstract Icon getIcon();

    protected abstract String getTooltipText();

    public static BuildStatusIcon createIcon(int remainingBrokenBuilds) {
        if (remainingBrokenBuilds == 0) {
            return new NoBrokenBuildIcon();
        }
        return new BrokenBuildIcon(remainingBrokenBuilds);
    }

    private BuildStatusIcon() {
        UIUtil.removeQuaquaVisualMarginsIn(this);
        setOpaque(false);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    protected void paintComponent(Graphics g) {

        g.setColor(UIUtil.getPanelBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        doPaintComponent(g);
    }

    protected abstract void doPaintComponent(Graphics g);


    protected void paintIcon(Graphics g, Icon icon, int x, int y) {
        icon.paintIcon(this, g, x, y);
    }


    private static class BrokenBuildIcon extends BuildStatusIcon {

        private static final Icon EXCLAMATION_ICON = GuiUtil.loadIcon("red.png");

        private static final String REMAINING_BROKEN_BUILD_MESSAGE = "%d remaining broken builds";

        private final int remainingBrokenBuilds;

        private static final int PIXEL_WIDTH = 8;

        private final int numberWith;


        private BrokenBuildIcon(int remainingBrokenBuilds) {
            this.remainingBrokenBuilds = remainingBrokenBuilds;
            numberWith = String.valueOf(remainingBrokenBuilds).length() * PIXEL_WIDTH;
        }

        @Override
        protected Icon getIcon() {
            return EXCLAMATION_ICON;
        }

        @Override
        protected String getTooltipText() {
            return String.format(REMAINING_BROKEN_BUILD_MESSAGE, remainingBrokenBuilds);
        }


        public Dimension getPreferredSize() {
            final Insets insets = getInsets();
            return new Dimension(
                    EXCLAMATION_ICON.getIconWidth() + insets.left + insets.right + numberWith,
                    EXCLAMATION_ICON.getIconHeight() + insets.top + insets.bottom
            );
        }

        protected void doPaintComponent(Graphics g) {
            final Dimension size = getSize();

            int x = (size.width - getIcon().getIconWidth() - numberWith) / 2;
            int y = (size.height - getIcon().getIconHeight()) / 2;
            paintIcon(g, getIcon(), x, y);
            setToolTipText(getTooltipText());

            Font originalFont = g.getFont();
            Color originalColor = g.getColor();
            g.setFont(calcFont());
            y += EXCLAMATION_ICON.getIconHeight() - g.getFontMetrics().getDescent();
            x += EXCLAMATION_ICON.getIconWidth();

            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(remainingBrokenBuilds), x, y);

            g.setFont(originalFont);
            g.setColor(originalColor);
        }

        private Font calcFont() {
            return getFont().deriveFont(Font.BOLD).deriveFont((float) EXCLAMATION_ICON.getIconHeight() * 3 / 5);
        }
    }

    private static class NoBrokenBuildIcon extends BuildStatusIcon {

        private static final Icon INFORMATION_ICON = GuiUtil.loadIcon("blue.png");

        private static final String NO_BROKEN_BUILDS = "No broken builds";

        @Override
        protected Icon getIcon() {
            return INFORMATION_ICON;
        }

        @Override
        protected String getTooltipText() {
            return NO_BROKEN_BUILDS;
        }

        public Dimension getPreferredSize() {
            final Insets insets = getInsets();
            return new Dimension(
                    getIcon().getIconWidth() + insets.left + insets.right,
                    getIcon().getIconHeight() + insets.top + insets.bottom
            );
        }

        protected void doPaintComponent(Graphics g) {
            final Dimension size = getSize();
            int x = (size.width - getIcon().getIconWidth()) / 2;
            int y = (size.height - getIcon().getIconHeight()) / 2;
            paintIcon(g, getIcon(), x, y);
            setToolTipText(getTooltipText());
        }
    }
}
