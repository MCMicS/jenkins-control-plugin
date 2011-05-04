package org.codinjustu.tools.jenkins.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjustu.tools.jenkins.logic.IdeaJenkinsBrowserLogic;
import org.codinjustu.tools.jenkins.util.GuiUtil;

public class CleanRssAction extends AnAction {
    private IdeaJenkinsBrowserLogic logic;


    public CleanRssAction(IdeaJenkinsBrowserLogic logic) {
        super("Clean last completed builds", "Clean last completed builds", GuiUtil.loadIcon("erase.png"));
        this.logic = logic;
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        logic.cleanRssEntries();
    }
}
