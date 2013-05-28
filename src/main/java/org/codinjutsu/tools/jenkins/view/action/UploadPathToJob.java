package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
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
        super("Upload a patch", "Upload a patch to the job", GuiUtil.loadIcon("execute.png"));
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        VirtualFile virtualFile = FileChooser.chooseFile(
                browserPanel,
                new FileChooserDescriptor(true, false, false, false, false, false)
        );
    }
}
