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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.panels.NonOpaquePanel;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.action.search.CloseJobSearchPanelAction;
import org.codinjutsu.tools.jenkins.view.action.search.NextOccurrenceAction;
import org.codinjutsu.tools.jenkins.view.action.search.PrevOccurrenceAction;
import org.codinjutsu.tools.jenkins.view.util.SearchTextField;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;

public class JobSearchComponent extends JPanel {

    private final JTextField searchField;

    private final JTree jobTree;
    private DefaultMutableTreeNode lastSelectedNode;

    public JobSearchComponent(JenkinsBrowserPanel jenkinsBrowserPanel, boolean installSearchToolbar) {
        this.jobTree = jenkinsBrowserPanel.getJobTree();

        setLayout(new BorderLayout());

        NonOpaquePanel searchSubPanel = new NonOpaquePanel();
        searchField = createSearchField();
        searchSubPanel.add(searchField);
        NonOpaquePanel closeSubPanel = new NonOpaquePanel();

        JComponent searchBarComponent;
        if (installSearchToolbar) {//TODO Crappy, need to decouple search toolbar creation from SearchComponent instanciation
            searchBarComponent = createSearchToolBar();
        } else {
            searchBarComponent = new JLabel();
        }

        closeSubPanel.add(createCloseButton());

        add(closeSubPanel, BorderLayout.EAST);
        add(searchBarComponent, BorderLayout.CENTER);
        add(searchSubPanel, BorderLayout.WEST);

        registerListeners();
    }

    private JTextField createSearchField() {
        JTextField searchField = new SearchTextField();
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
                searchField.requestFocus(true);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        new CloseJobSearchPanelAction(this);
    }


    public JComponent createSearchToolBar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup("search bar", false);
        actionGroup.add(new PrevOccurrenceAction(this));
        actionGroup.add(new NextOccurrenceAction(this));

        ActionToolbar searchBar = ActionManager.getInstance().createActionToolbar("SearchBar", actionGroup, true);
        searchBar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        JComponent searchBarComponent = searchBar.getComponent();
        searchBarComponent.setBorder(null);
        searchBarComponent.setOpaque(false);
        return searchBarComponent;
    }

    private Component createCloseButton() {
        JLabel closeLabel = new JLabel();
        closeLabel.setIcon(GuiUtil.loadIcon("close.png"));
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

        SearchMovement forwardMovement = new SearchMovement() {
            public DefaultMutableTreeNode get(DefaultMutableTreeNode node) {
                return node.getNextNode();
            }
        };

        findOccurrence(text, forwardMovement);
    }

    public void findPreviousOccurrence(String text) {

        SearchMovement backwardMovement = new SearchMovement() {
            public DefaultMutableTreeNode get(DefaultMutableTreeNode node) {
                return node.getPreviousNode();
            }
        };

        findOccurrence(text, backwardMovement);
    }

    private void findOccurrence(String text, SearchMovement forwardMovement) {
        DefaultMutableTreeNode currentNode = getStartingNode(forwardMovement);
        boolean foundNode = false;
        while (currentNode != null) {
            Object userObject = currentNode.getUserObject();
            if (userObject instanceof Jenkins) {
                break;
            }
            Job job = (Job) userObject;
            if (StringUtils.startsWithIgnoreCase(job.getName(), text) && !foundNode) {
                lastSelectedNode = currentNode;
                TreePath pathToSelect = new TreePath(currentNode.getPath());
                jobTree.setSelectionPath(pathToSelect);
                jobTree.scrollPathToVisible(pathToSelect);
                foundNode = true;
            }
            currentNode = forwardMovement.get(currentNode);
        }

        if (foundNode) {
// TODO            System.out.println("No more found" );
        }
    }


    private DefaultMutableTreeNode getStartingNode(SearchMovement forwardMovement) {
        if (lastSelectedNode != null) {
            return forwardMovement.get(lastSelectedNode);
        }

        TreeModel model = jobTree.getModel();
        Object rootNode = model.getRoot();
        if (model.getChildCount(rootNode) == 0) {
            return null;
        }
        return (DefaultMutableTreeNode) model.getChild(rootNode, 0);

    }

    public JTextField getSearchField() {
        return searchField;
    }

    public void resetSearch() {
        searchField.setText("");
        lastSelectedNode = null;
    }

    interface SearchMovement {

        DefaultMutableTreeNode get(DefaultMutableTreeNode node);
    }

    public boolean hasMatches() {
        return lastSelectedNode != null;
    }


}
