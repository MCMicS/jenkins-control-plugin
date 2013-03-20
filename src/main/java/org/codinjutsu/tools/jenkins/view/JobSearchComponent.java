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

import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.panels.NonOpaquePanel;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.action.search.CloseJobSearchPanelAction;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;

public class JobSearchComponent extends JPanel {

    private static final Icon CLOSE_ICON = GuiUtil.isUnderDarcula() ? GuiUtil.loadIcon("close_dark.png") : GuiUtil.loadIcon("close.png");

    private final JTextField searchField;
    private final JLabel infoLabel;
    private final JTree jobTree;
    private final Color defaultBackground;

    private DefaultMutableTreeNode lastSelectedNode;

    public JobSearchComponent(JTree jobTree) {
        this.jobTree = jobTree;

        setLayout(new BorderLayout());

        NonOpaquePanel searchSubPanel = new NonOpaquePanel();
        searchField = createSearchField();
        defaultBackground = searchField.getBackground();
        searchSubPanel.add(searchField);
        add(searchSubPanel, BorderLayout.WEST);

        NonOpaquePanel closeSubPanel = new NonOpaquePanel();
        infoLabel = createLabelWithBoldFont();
        closeSubPanel.add(infoLabel, BorderLayout.WEST);
        closeSubPanel.add(createCloseButton());
        add(closeSubPanel, BorderLayout.EAST);

        registerListeners();
    }

    private JLabel createLabelWithBoldFont() {
        JLabel label = new JLabel();
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    public void installSearchToolBar(ActionToolbar searchBar) {

        searchBar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        JComponent searchBarComponent = searchBar.getComponent();
        searchBarComponent.setBorder(null);
        searchBarComponent.setOpaque(false);

        add(searchBarComponent, BorderLayout.CENTER);
    }


    private JTextField createSearchField() {
        JTextField searchField = new JTextField();
        searchField.setColumns(15);
        searchField.setName("searchField");
        return searchField;
    }


    private void registerListeners() {
        searchField.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                String text = searchField.getText();
                if (StringUtil.isEmpty(text)) {
                    return;
                }

                findNextOccurrence(text);
                searchField.requestFocus();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        new CloseJobSearchPanelAction(this);
    }

    private Component createCloseButton() {
        JLabel closeLabel = new JLabel();
        closeLabel.setIcon(CLOSE_ICON);
        closeLabel.setToolTipText("Close search bar (Escape)");
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                JobSearchComponent.this.setVisible(false);
            }
        });
        return closeLabel;
    }

    public void forceFocusForSearchTextField() {
        searchField.requestFocus();
    }


    public void findNextOccurrence(String text) {

        findOccurrence(text, new SearchMovement() {
            public DefaultMutableTreeNode get(DefaultMutableTreeNode node) {
                return node.getNextNode();
            }

            @Override
            public DefaultMutableTreeNode getFirst(TreeModel model, DefaultMutableTreeNode rootNode) {
                return (DefaultMutableTreeNode) model.getChild(rootNode, 0);
            }


        });
    }

    public void findPreviousOccurrence(String text) {

        findOccurrence(text, new SearchMovement() {
            public DefaultMutableTreeNode get(DefaultMutableTreeNode node) {
                return node.getPreviousNode();
            }

            @Override
            public DefaultMutableTreeNode getFirst(TreeModel model, DefaultMutableTreeNode rootNode) {
                return (DefaultMutableTreeNode) model.getChild(rootNode, (model.getChildCount(rootNode) - 1));
            }
        });
    }

    private void findOccurrence(String text, SearchMovement searchMovement) {
        DefaultMutableTreeNode currentNode = getStartingNode(searchMovement);
        boolean foundNode = false;
        while (currentNode != null) {
            Object userObject = currentNode.getUserObject();
            if (userObject instanceof Jenkins) {
                break;
            }
            Job job = (Job) userObject;
            if (StringUtils.containsIgnoreCase(job.getName(), text) && !foundNode) {
                lastSelectedNode = currentNode;
                TreePath pathToSelect = new TreePath(currentNode.getPath());
                jobTree.setSelectionPath(pathToSelect);
                jobTree.scrollPathToVisible(pathToSelect);
                foundNode = true;
            }
            currentNode = searchMovement.get(currentNode);
        }

        updateFeebackComponent(foundNode);
    }

    private void updateFeebackComponent(boolean foundNode) {
        searchField.setBackground(!foundNode ? MessageType.ERROR.getPopupBackground() : defaultBackground);
        infoLabel.setText(!foundNode ? "No more matches" : "");
        if (!foundNode) {
            lastSelectedNode = null;
        }
    }


    private DefaultMutableTreeNode getStartingNode(SearchMovement searchMovement) {
        if (lastSelectedNode != null) {
            return searchMovement.get(lastSelectedNode);
        }

        TreeModel model = jobTree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        if (model.getChildCount(rootNode) == 0) {
            return null;
        }
        return searchMovement.getFirst(model, rootNode);

    }

    public JTextField getSearchField() {
        return searchField;
    }

    public void resetSearch() {
        searchField.setText("");
        updateFeebackComponent(true);
        lastSelectedNode = null;
    }

    interface SearchMovement {

        DefaultMutableTreeNode get(DefaultMutableTreeNode node);

        DefaultMutableTreeNode getFirst(TreeModel model, DefaultMutableTreeNode rootNode);
    }
}
