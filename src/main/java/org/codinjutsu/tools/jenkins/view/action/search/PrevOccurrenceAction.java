package org.codinjutsu.tools.jenkins.view.action.search;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.JobSearchComponent;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class PrevOccurrenceAction extends AnAction implements DumbAware {

    private JobSearchComponent jobSearchComponent;

    public PrevOccurrenceAction(JobSearchComponent jobSearchComponent) {
        super("Search Previous", "Search the previous occurrence", GuiUtil.loadIcon("previous.png"));
        this.jobSearchComponent = jobSearchComponent;

        registerCustomShortcutSet(KeyEvent.VK_F3, InputEvent.SHIFT_MASK, jobSearchComponent);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        jobSearchComponent.findPreviousOccurrence(jobSearchComponent.getSearchField().getText());
    }

    @Override
    public void update(final AnActionEvent e) {
        e.getPresentation().setEnabled(jobSearchComponent.hasMatches());
    }

}
