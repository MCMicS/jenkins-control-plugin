package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

/**
 * Description
 *
 * @author Yuri Novitsky
 */
public class UploadPathToJob extends AnAction implements DumbAware {

    private BrowserPanel browserPanel;

    public UploadPathToJob(BrowserPanel browserPanel) {
        super("Upload patch", "Upload patch to the job", GuiUtil.loadIcon("star_add.png"));
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
