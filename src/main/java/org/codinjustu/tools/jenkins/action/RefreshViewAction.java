package org.codinjustu.tools.jenkins.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjustu.tools.jenkins.logic.IdeaJenkinsBrowserLogic;
import org.codinjustu.tools.jenkins.model.Jenkins;
import org.codinjustu.tools.jenkins.util.GuiUtil;

public class RefreshViewAction extends AnAction {

    private IdeaJenkinsBrowserLogic logic;


    public RefreshViewAction(IdeaJenkinsBrowserLogic logic) {
        super("Refresh", "Refresh current view", GuiUtil.loadIcon("loadingTree.png"));
        this.logic = logic;
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        if (logic.getSelectedJob() != null) {
            logic.loadSelectedJob();
        } else {
            logic.loadSelectedView();
        }
    }


    @Override
    public void update(AnActionEvent event) {
        Jenkins jenkins = logic.getJenkins();
        event.getPresentation().setEnabled(jenkins != null || logic.getSelectedJob() != null);
    }


}
