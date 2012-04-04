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

import org.codinjutsu.tools.jenkins.model.View;

import javax.swing.*;
import java.awt.*;

class JenkinsNestedViewComboRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof View) {
            View view = (View) value;
            if (view.hasNestedView()) {
                Component comp = super.getListCellRendererComponent(list, view.getName(), index, isSelected, cellHasFocus);
                comp.setEnabled(false);
                comp.setFocusable(false);
                comp.setBackground(Color.LIGHT_GRAY);
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                return comp;
            } else {
                String viewName = view.getName();
                if (view.isNested()) {
                    return super.getListCellRendererComponent(list, "   " + viewName, index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, viewName, index, isSelected, cellHasFocus);
                }
            }
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }


}
