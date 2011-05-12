package org.codinjutsu.tools.jenkins.view;

import com.intellij.ide.BrowserUtil;
import com.intellij.ui.HyperlinkLabel;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public class BuildResultPanel extends JPanel {
    private JButton closeButton;
    private JPanel rootPanel;
    private JPanel buildStatusPanel;


    public BuildResultPanel(String jobName, String buildMessage, Icon icon, final String buildUrl) {
        buildStatusPanel.setLayout(new BorderLayout());

        HyperlinkLabel buildStatusLabel = new HyperlinkLabel(buildMessage);
        buildStatusLabel.setIcon(icon);
        buildStatusLabel.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(final HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    BrowserUtil.launchBrowser(buildUrl);
                }
            }
        });

        buildStatusPanel.add(buildStatusLabel, BorderLayout.CENTER);

        closeButton.setIcon(GuiUtil.loadIcon("cross.png"));

        rootPanel.setBorder(BorderFactory.createTitledBorder(jobName));

        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
    }


    public JButton getCloseButton() {
        return closeButton;
    }
}
