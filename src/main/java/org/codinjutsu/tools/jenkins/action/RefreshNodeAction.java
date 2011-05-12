package org.codinjutsu.tools.jenkins.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.jenkins.logic.IdeaJenkinsBrowserLogic;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

public class RefreshNodeAction extends AnAction {

    private IdeaJenkinsBrowserLogic logic;


    public RefreshNodeAction(IdeaJenkinsBrowserLogic logic) {
        super("Refresh", "Refresh current node", GuiUtil.loadIcon("loadingTree.png"));
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
