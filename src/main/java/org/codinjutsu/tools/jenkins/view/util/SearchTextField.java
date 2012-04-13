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

import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

//Note from the author: This class was originally cloned from https://github.com/codjo/codjo-gui-toolkit
public class SearchTextField extends JTextField {
    private final Icon imageIcon;
    private static final int ARC_SIZE = 10;
    private static final Color FROM_COLOR = Color.GRAY;
    private static final Color TO_COLOR = new Color(220, 220, 220);


    public SearchTextField() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(3, 20, 3, 10));
        setColumns(15);
        imageIcon = GuiUtil.loadIcon("find.png");
    }


    @Override
    public void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;

        RenderingHints hints = graphics2D.getRenderingHints();
        boolean antialiasOn = hints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
        if (!antialiasOn) {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        int width = getWidth();
        int height = getHeight();
        Paint storedPaint = graphics2D.getPaint();

        //outer rectangle
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRoundRect(0, 0, width, height, ARC_SIZE, ARC_SIZE);

        //inner rectangle
        graphics2D.setPaint(new GradientPaint(width / 2, 0, FROM_COLOR, width / 2, height / 2, TO_COLOR));
        graphics2D.setColor(TO_COLOR);
        graphics2D.setStroke(new BasicStroke(1.7f));
        graphics2D.drawRoundRect(0, 0, width - 1, height - 1, ARC_SIZE, ARC_SIZE);

        graphics2D.setPaint(storedPaint);

        //draw search icon on the left
        imageIcon.paintIcon(this, graphics2D, 3, 3);

        super.paintComponent(graphics);

        if (!antialiasOn) {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }
}

