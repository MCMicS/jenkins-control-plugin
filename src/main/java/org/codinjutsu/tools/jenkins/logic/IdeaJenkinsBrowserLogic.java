package org.codinjutsu.tools.jenkins.logic;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.PopupHandler;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.action.*;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

public class IdeaJenkinsBrowserLogic extends JenkinsBrowserLogic<JenkinsBrowserPanel> {

    private static final String JENKINS_JOB_ACTION_GROUP = "JenkinsJobGroup";
    private static final String JENKINS_RSS_ACTIONS = "JenkinsRssActions";
    private static final String JENKINS_ACTIONS = "jenkinsBrowserActions";


    public IdeaJenkinsBrowserLogic(JenkinsConfiguration configuration,
                                   JenkinsRequestManager jenkinsRequestManager) {
        super(configuration, jenkinsRequestManager, new JenkinsBrowserPanel());
    }


    @Override
    protected void initView() {

        installRssActions(getView().getRssActionPanel());
        installBrowserActions(getView().getJobTree(), getView().getActionPanel());

        initListeners();
    }

    @Override
    protected void displayConnectionErrorMsg() {
        getView().setErrorMsg();
    }


    @Override
    protected void showErrorDialog(String errorMessage, String title) {
        getView().showErrorDialog(errorMessage, title);
    }


    @Override
    protected void displayFinishedBuilds(Map<String, Build> displayableFinishedBuilds) {
        getView().getRssLatestJobPanel().addFinishedBuild(displayableFinishedBuilds);
    }


    @Override
    public void cleanRssEntries() {
        getView().getRssLatestJobPanel().cleanRssEntries();
    }


    void installRssActions(JPanel rssActionPanel) {
        DefaultActionGroup actionGroup = new DefaultActionGroup(JENKINS_RSS_ACTIONS, true);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(new RefreshRssAction(this));
            actionGroup.addSeparator();
            actionGroup.add(new CleanRssAction(this));
        }
        installActionGroupInToolBar(actionGroup, rssActionPanel, ActionManager.getInstance(), JENKINS_RSS_ACTIONS);
    }


    void installBrowserActions(JTree jobTree, JPanel toolBar) {
        DefaultActionGroup actionGroup = new DefaultActionGroup(JENKINS_JOB_ACTION_GROUP, true);
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(new RefreshViewAction(this));
            actionGroup.addSeparator();
            actionGroup.add(new JenkinsBuildAction(this));
            actionGroup.addSeparator();
            actionGroup.add(new GotoJobPageAction(getView()));
            actionGroup.add(new GotoLastBuildPageAction(getView()));
        }
        installActionGroupInToolBar(actionGroup, toolBar, ActionManager.getInstance(), JENKINS_ACTIONS);
        installActionGroupInPopupMenu(actionGroup, jobTree, ActionManager.getInstance());
    }


    public Jenkins getJenkins() {
        return getView().getJenkins();
    }


    private static void installActionGroupInPopupMenu(ActionGroup group,
                                                      JComponent component,
                                                      ActionManager actionManager) {
        if (actionManager == null) {
            return;
        }
        PopupHandler.installPopupHandler(component, group, "POPUP", actionManager);
    }


    private static void installActionGroupInToolBar(ActionGroup actionGroup,
                                                    JComponent component,
                                                    ActionManager actionManager, String toolBarName) {
        if (actionManager == null) {
            return;
        }

        JComponent actionToolbar = ActionManager.getInstance()
                .createActionToolbar(toolBarName, actionGroup, true).getComponent();
        component.add(actionToolbar, BorderLayout.CENTER);
    }

    private void initListeners() {
        getView().getViewCombo().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    loadSelectedView();
                }
            }
        });
    }
}
