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
