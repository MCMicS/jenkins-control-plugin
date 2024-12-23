package org.codinjutsu.tools.jenkins.view.inputfilter;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.intellij.util.ui.StatusText;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import static javax.swing.SwingConstants.TOP;

public class InputFilterList extends JComponent {

    private final JTextField filterTextField = new HintTextField("Search");
    private final BiPredicate<String, String> userFilter;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JBList<String> list = new JBList<>(model);
    private final JBScrollPane scrollPane = new JBScrollPane(this.list);

    private final List<String> itemList;
    private final String defaultValue;

    private String filteringText = "";

    public InputFilterList(String defaultValue, List<String> itemList, BiPredicate<String, String> userFilter) {
        this.itemList = itemList;
        this.userFilter = userFilter;
        this.defaultValue = defaultValue;

        setLayout(new HorizontalLayout(0, TOP));

        model.addAll(itemList);
        list.setSelectedValue(defaultValue, true);

        initDefaultEmptyTextIfNeed();
        initUIPreferredSize();
        initDocumentListener();
        initKeyListener();
    }

    public String getSelectedItem() {
        String currentSelectedValue = list.getSelectedValue();
        return currentSelectedValue != null ? currentSelectedValue : defaultValue;
    }


    private void initDefaultEmptyTextIfNeed() {
        if (defaultValue != null) {
            StatusText emptyText = list.getEmptyText();
            emptyText.setShowAboveCenter(false);
            emptyText.setText("No result");
            emptyText.appendLine(String.format("(Default value : %s)", defaultValue));
        }
    }

    private void initUIPreferredSize() {
        scrollPane.setPreferredSize(new Dimension(250, 100));
        add(scrollPane);

        filterTextField.setPreferredSize(new Dimension(80, filterTextField.getPreferredSize().height));
        add(filterTextField);
    }

    private void initKeyListener() {
        filterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_KP_UP) {
                    moveSelectedIndex(true);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_KP_DOWN) {
                    moveSelectedIndex(false);
                    e.consume();
                }
            }
        });
    }

    private void moveSelectedIndex(boolean isUp) {
        int currentIndex = list.getSelectedIndex();
        if (currentIndex == -1) return;
        if (isUp && currentIndex == 0) return;
        if (!isUp && currentIndex == model.size() - 1) return;
        list.setSelectedIndex(isUp ? currentIndex - 1 : currentIndex + 1);
        list.ensureIndexIsVisible(isUp ? currentIndex - 1 : currentIndex + 1);
    }

    private void initDocumentListener() {
        filterTextField.getDocument().addDocumentListener(new DocumentAdapter() {

            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    applyFilter();
                    filteringText = filterTextField.getText();
                });
            }
        });
    }

    private void applyFilter() {
        if (Objects.equals(filterTextField.getText(), filteringText)) {
            return;
        }
        model.removeAllElements();

        final ArrayList<String> filteredList = new ArrayList<>();
        boolean hasItem = false;
        for (String item : itemList) {
            if (userFilter.test(item, filterTextField.getText())) {
                hasItem = true;
                filteredList.add(item);
            }
        }

        model.addAll(filteredList);
        if (hasItem) {
            list.setSelectedIndex(0);
            list.ensureIndexIsVisible(0);
        } else {
            list.clearSelection();
        }
        filterTextField
                .setForeground(!hasItem ? JBColor.RED : UIManager.getColor("Label.foreground"));
    }

}
