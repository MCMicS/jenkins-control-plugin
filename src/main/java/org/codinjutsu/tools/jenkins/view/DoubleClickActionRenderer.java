package org.codinjutsu.tools.jenkins.view;

import com.intellij.ui.components.JBLabel;
import org.codinjutsu.tools.jenkins.DoubleClickAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DoubleClickActionRenderer implements ListCellRenderer<DoubleClickAction> {

    private final JLabel label = new JBLabel();

    @Override
    public Component getListCellRendererComponent(JList<? extends DoubleClickAction> list, DoubleClickAction value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        label.setText(getDisplayValue(value));
        label.setFont(label.getFont().deriveFont(value == DoubleClickAction.DEFAULT ? Font.BOLD : Font.PLAIN));
        return label;
    }

    @NotNull
    String getDisplayValue(@NotNull DoubleClickAction doubleClickAction) {
        final String displayName;
        switch (doubleClickAction) {
            case TRIGGER_BUILD:
                displayName = "Build on Jenkins";
                break;
            case LOAD_BUILDS:
                displayName = "Load Builds";
                break;
            case SHOW_LAST_LOG:
                displayName = "Show last log";
                break;
            default:
                displayName = doubleClickAction.toString();
        }

        return displayName;
    }
}
