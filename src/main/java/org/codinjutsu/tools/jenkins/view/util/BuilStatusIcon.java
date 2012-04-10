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

public class BuilStatusIcon extends JComponent {

    private static final Icon SMALL_PASSIVE_ICON = GuiUtil.loadIcon("jenkins_logo.png");

    public BuilStatusIcon() {
        UIUtil.removeQuaquaVisualMarginsIn(this);

        setOpaque(false);
    }

    public Dimension getPreferredSize() {
        final Insets insets = getInsets();
        return new Dimension(SMALL_PASSIVE_ICON.getIconWidth() + insets.left + insets.right, SMALL_PASSIVE_ICON.getIconHeight() + insets.top + insets.bottom);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    protected void paintComponent(Graphics g) {

        if (isOpaque()) {
            final Container parent = getParent();
            JComponent opaque = null;
            if (parent instanceof JComponent) {
                opaque = (JComponent) UIUtil.findNearestOpaque((JComponent) parent);
            }
            Color bg = opaque != null ? opaque.getBackground() : UIUtil.getPanelBackground();
            g.setColor(bg);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        final Dimension size = getSize();
        int x = (size.width - SMALL_PASSIVE_ICON.getIconWidth()) / 2;
        int y = (size.height - SMALL_PASSIVE_ICON.getIconHeight()) / 2;

        paintIcon(g, SMALL_PASSIVE_ICON, x, y);
    }

    protected void paintIcon(Graphics g, Icon icon, int x, int y) {
        icon.paintIcon(this, g, x, y);
    }

}
