package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.model.View;

import javax.swing.*;
import java.awt.*;

class JenkinsViewComboRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        if (value instanceof View) {
            View view = (View) value;
            return super.getListCellRendererComponent(list, view.getName(), index, isSelected, cellHasFocus);
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
