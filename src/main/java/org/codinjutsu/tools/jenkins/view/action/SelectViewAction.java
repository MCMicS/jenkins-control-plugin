/*
 * Copyright (c) 2013 David Boissier
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

package org.codinjutsu.tools.jenkins.view.action;

//import com.intellij.icons.AllIcons;
//import com.intellij.ide.DataManager;
//import com.intellij.openapi.actionSystem.*;
//import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
//import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
//import com.intellij.openapi.project.DumbAwareAction;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.ui.popup.JBPopup;
//import com.intellij.openapi.ui.popup.JBPopupFactory;
//import com.intellij.openapi.ui.popup.PopupChooserBuilder;
//import com.intellij.ui.ClickListener;
//import com.intellij.ui.awt.RelativePoint;
//import com.intellij.ui.components.JBList;
//import com.intellij.util.Consumer;
//import com.intellij.util.ui.UIUtil;
//import org.codinjutsu.tools.jenkins.logic.BrowserLogic;
//import org.codinjutsu.tools.jenkins.model.View;
//import org.codinjutsu.tools.jenkins.view.JenkinsViewComboRenderer;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.util.List;
//
//public class SelectViewAction extends DumbAwareAction implements CustomComponentAction {
//
//
//    private final BrowserLogic browserLogic;
//
//    private JPanel myPanel;
//
//    private JLabel viewLabel;
//
//    public SelectViewAction(BrowserLogic browserLogic) {
//        this.browserLogic = browserLogic;
//
//        myPanel = new JPanel(new BorderLayout());
//        viewLabel = new JLabel();
//        myPanel.add(viewLabel, BorderLayout.CENTER);
//
//    }
//
//    @Override
//    public void update(AnActionEvent e) {
//        View currentSelectedView = browserLogic.getCurrentSelectedView();
//        if (currentSelectedView != null) {
//            viewLabel.setText(currentSelectedView.getName());
//        }
//    }
//
//    @Override
//    public void actionPerformed(AnActionEvent e) {
//        List<View> views = browserLogic.getJenkins().getViews();
//        if (views.isEmpty()) {
//            return;
//        }
//
//        final JBList viewList = new JBList(views);
//        viewList.setCellRenderer(new JenkinsViewComboRenderer());
//        new PopupChooserBuilder(viewList)
//                .setMovable(false)
//                .setCancelKeyEnabled(true)
//                .setItemChoosenCallback(new Runnable() {
//                    public void run() {
//                        final View view = (View) viewList.getSelectedValue();
//                        if (view == null) return;
//
//                        browserLogic.loadView(view);
//                    }
//                })
//                .createPopup()
//                .show(JBPopupFactory.getInstance().guessBestPopupLocation(e.getDataContext()));
//    }
//
//    @Override
//    public JComponent createCustomComponent(Presentation presentation) {
//        return myPanel;
//    }
//}

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import org.codinjutsu.tools.jenkins.model.FavoriteView;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.JenkinsNestedViewComboRenderer;
import org.codinjutsu.tools.jenkins.view.JenkinsViewComboRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * Note: Inspiration from git4idea.history.wholeTree.BasePopupAction (git4idea plugin)
 */
public class SelectViewAction extends DumbAwareAction implements CustomComponentAction {

    private static final Icon ARROWS_ICON = AllIcons.Ide.Statusbar_arrows;

    protected final JLabel myLabel;
    protected final JPanel myPanel;
    private final BrowserPanel browserPanel;

    public SelectViewAction(final BrowserPanel browserPanel) {
        this.browserPanel = browserPanel;
        myPanel = new JPanel();
        final BoxLayout layout = new BoxLayout(myPanel, BoxLayout.X_AXIS);
        myPanel.setLayout(layout);
        myLabel = new JLabel();
        final JLabel show = new JLabel("View:");
        show.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
        myPanel.add(show);
        myPanel.add(myLabel);
        myPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 3));
        final JLabel iconLabel = new JLabel(ARROWS_ICON);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        myPanel.add(iconLabel, myLabel);

        myPanel.addMouseListener(new MyMouseAdapter());
    }

    private JBList<View> buildViewList(List<View> views, BrowserPanel browserPanel) {
        List<View> unflattenViews = flatViewList(views);

        if (browserPanel.hasFavoriteJobs()) {
            unflattenViews.add(FavoriteView.create());
        }

        final JBList<View> viewList = new JBList<>(unflattenViews);

        if (hasNestedViews(unflattenViews)) {
            viewList.setCellRenderer(new JenkinsNestedViewComboRenderer());
        } else {
            viewList.setCellRenderer(new JenkinsViewComboRenderer());
        }
        return viewList;
    }


    @Override
    public void update(AnActionEvent e) {
        View currentSelectedView = browserPanel.getCurrentSelectedView();
        if (currentSelectedView != null) {
            myLabel.setText(currentSelectedView.getName());
        } else {
            myLabel.setText("");
        }
    }

    @NotNull
    @Override
    public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        return myPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
    }


    private static List<View> flatViewList(List<View> views) {
        List<View> flattenViewList = new LinkedList<View>();
        for (View view : views) {
            flattenViewList.add(view);
            if (view.hasNestedView()) {
                for (View subView : view.getSubViews()) {
                    flattenViewList.add(subView);
                }
            }
        }

        return flattenViewList;
    }


    private static boolean hasNestedViews(List<View> views) {
        for (View view : views) {
            if (view.hasNestedView()) return true;
        }
        return false;
    }

    private class MyMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            List<View> views = browserPanel.getJenkins().getViews();
            if (views.isEmpty()) {
                return;
            }

            final JBList<View> viewList = buildViewList(views, browserPanel);

            JBPopup popup = new PopupChooserBuilder<View>(viewList)
                    .setMovable(false)
                    .setCancelKeyEnabled(true)
                    .setItemChoosenCallback(() -> {
                        final View view = viewList.getSelectedValue();
                        if (view == null || view.hasNestedView()) return;
                        browserPanel.loadView(view);
                    })
                    .createPopup();

            if (e != null) {
                popup.show(new RelativePoint(e));
            } else {
                final Dimension dimension = popup.getContent().getPreferredSize();
                final Point at = new Point(-dimension.width / 2, -dimension.height);
                popup.show(new RelativePoint(myLabel, at));
            }
        }
    }
}

