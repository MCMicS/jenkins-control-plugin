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
import java.awt.event.MouseAdapter;

public abstract class BuildStatusIcon extends JComponent {


    protected abstract Icon getIcon();

    protected abstract String getTooltipText();

    public static JComponent createIcon(int nbBrokenBuilds, int nbUnstableBuilds, MouseAdapter mouseAdapter) {
        //TODO crappy need refactor this

        if (nbBrokenBuilds == 0 && nbUnstableBuilds == 0) {
            NoBrokenBuildIcon noBrokenBuildIcon = new NoBrokenBuildIcon();
            noBrokenBuildIcon.addMouseListener(mouseAdapter);
            return noBrokenBuildIcon;
        }
        BrokenBuildIcon brokenBuildIcon = new BrokenBuildIcon(nbBrokenBuilds);
        brokenBuildIcon.addMouseListener(mouseAdapter);
        if (nbBrokenBuilds > 0 && nbUnstableBuilds == 0) {
            return brokenBuildIcon;
        }
        UnstableBuildIcon unstableBuildIcon = new UnstableBuildIcon(nbUnstableBuilds);
        unstableBuildIcon.addMouseListener(mouseAdapter);
        if (nbBrokenBuilds == 0 && nbUnstableBuilds > 0) {
            return unstableBuildIcon;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(brokenBuildIcon);
        panel.add(unstableBuildIcon);
        return panel;
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


    private static class UnstableBuildIcon extends AbstractUnSuccessfullIcon {

        private static final Icon YELLOW_ICON = GuiUtil.loadIcon("yellow.png");

        private static final String NB_UNSTABLE_BUILD_MESSAGE = "%d remaining unstable builds";

        private UnstableBuildIcon(int nbUnstableBuilds) {
            super(nbUnstableBuilds);
        }

        @Override
        protected String getTooltipTemplateText() {
            return NB_UNSTABLE_BUILD_MESSAGE;
        }

        @Override
        protected Icon getIcon() {
            return YELLOW_ICON;
        }
    }


    private static class BrokenBuildIcon extends AbstractUnSuccessfullIcon {

        private static final Icon EXCLAMATION_ICON = GuiUtil.loadIcon("red.png");

        private static final String REMAINING_BROKEN_BUILD_MESSAGE = "%d remaining broken builds";

        protected BrokenBuildIcon(int nbUnsuccessfulBuilds) {
            super(nbUnsuccessfulBuilds);
        }

        @Override
        protected String getTooltipTemplateText() {
            return REMAINING_BROKEN_BUILD_MESSAGE;
        }

        @Override
        protected Icon getIcon() {
            return EXCLAMATION_ICON;
        }
    }

    private static abstract class AbstractUnSuccessfullIcon extends BuildStatusIcon {


        private final int remainingBrokenBuilds;

        private static final int PIXEL_WIDTH = 8;

        private final int numberWith;


        protected AbstractUnSuccessfullIcon(int nbUnsuccessfulBuilds) {
            this.remainingBrokenBuilds = nbUnsuccessfulBuilds;
            numberWith = String.valueOf(nbUnsuccessfulBuilds).length() * PIXEL_WIDTH;
        }


        @Override
        protected String getTooltipText() {
            return String.format(getTooltipTemplateText(), remainingBrokenBuilds);
        }

        protected abstract String getTooltipTemplateText();


        public Dimension getPreferredSize() {
            final Insets insets = getInsets();
            return new Dimension(
                    getIcon().getIconWidth() + insets.left + insets.right + numberWith,
                    getIcon().getIconHeight() + insets.top + insets.bottom
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
            y += getIcon().getIconHeight() - g.getFontMetrics().getDescent();
            x += getIcon().getIconWidth();

            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(remainingBrokenBuilds), x, y);

            g.setFont(originalFont);
            g.setColor(originalColor);
        }

        private Font calcFont() {
            return getFont().deriveFont(Font.BOLD).deriveFont((float) getIcon().getIconHeight() * 3 / 5);
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
