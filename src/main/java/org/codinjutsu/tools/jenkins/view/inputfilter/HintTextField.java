package org.codinjutsu.tools.jenkins.view.inputfilter;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

class HintTextField extends JTextField implements FocusListener {

    private final String hint;
    private final JBColor hintColor = JBColor.gray;

    private Color originalColor;
    private boolean showingHint;

    public HintTextField(final String hint) {
        super(hint);
        this.hint = hint;
        this.showingHint = true;
        this.originalColor = this.getForeground();
        super.setForeground(hintColor);
        super.addFocusListener(this);
    }

    @Override
    public void setForeground(Color fg) {
        originalColor = fg;
        super.setForeground(fg);
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        if (!t.isEmpty()) {
            super.setForeground(originalColor);
            showingHint = false;
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setForeground(originalColor);
            super.setText("");
            showingHint = false;
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setForeground(hintColor);
            super.setText(hint);
            showingHint = true;
        }
    }

    @Override
    public String getText() {
        return showingHint ? "" : super.getText();
    }
}