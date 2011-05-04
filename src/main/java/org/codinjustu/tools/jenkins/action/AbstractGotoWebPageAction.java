package org.codinjustu.tools.jenkins.action;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.codinjustu.tools.jenkins.util.GuiUtil;
import org.codinjustu.tools.jenkins.view.JenkinsBrowserView;

abstract class AbstractGotoWebPageAction extends AnAction {
    final JenkinsBrowserView jenkinsBrowserPanel;


    AbstractGotoWebPageAction(String label,
                              String description,
                              String iconFilename,
                              JenkinsBrowserView jenkinsBrowserPanel) {
        super(label, description, GuiUtil.loadIcon(iconFilename));
        this.jenkinsBrowserPanel = jenkinsBrowserPanel;
    }


    protected abstract String getUrl();


    @Override
    public void actionPerformed(AnActionEvent event) {
        BrowserUtil.launchBrowser(getUrl());
    }
}
