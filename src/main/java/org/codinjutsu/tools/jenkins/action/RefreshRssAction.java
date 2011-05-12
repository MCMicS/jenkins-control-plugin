package org.codinjutsu.tools.jenkins.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjutsu.tools.jenkins.logic.IdeaJenkinsBrowserLogic;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

public class RefreshRssAction extends AnAction {

    private IdeaJenkinsBrowserLogic logic;


    public RefreshRssAction(IdeaJenkinsBrowserLogic logic) {
        super("Refresh last completed builds", "Refresh last completed builds from Jenkins server", GuiUtil.loadIcon("rss.png"));
        this.logic = logic;
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        logic.refreshLatestCompletedBuilds();
    }
}
