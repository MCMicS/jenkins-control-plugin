package org.codinjutsu.tools.jenkins.view.action.search;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.SystemInfo;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.JobSearchComponent;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class NextOccurrenceAction extends AnAction implements DumbAware {

    private JobSearchComponent jobSearchComponent;

    public NextOccurrenceAction(JobSearchComponent jobSearchComponent) {
        super("Search Next", "Search the next occurrence", GuiUtil.loadIcon("next.png"));
        this.jobSearchComponent = jobSearchComponent;

        registerCustomShortcutSet(KeyEvent.VK_F3, 0, jobSearchComponent);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        jobSearchComponent.findNextOccurrence(jobSearchComponent.getSearchField().getText());
    }

    @Override
    public void update(final AnActionEvent e) {
        e.getPresentation().setEnabled(jobSearchComponent.hasMatches());
    }

}
