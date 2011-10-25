/*
 * Copyright (c) 2011 David Boissier
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

import javax.swing.*;
import java.awt.*;

class CompositeIcon extends ImageIcon {

    private final Icon[] icons;

    CompositeIcon(Icon... icons) {
        this.icons = icons;
    }

    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        int shift = 0;
        for (Icon imageIcon : icons) {
            imageIcon.paintIcon(c, g, x + shift, y);
            shift += imageIcon.getIconWidth();
        }
    }

    public int getIconHeight() {
        int height = 0;
        for (Icon imageIcon : icons) {
            height = Math.max(height, imageIcon.getIconHeight());
        }
        return height;
    }

    public int getIconWidth() {
        int width = 0;
        for (Icon imageIcon : icons) {
            width += imageIcon.getIconWidth();
        }
        return width;

    }
}



