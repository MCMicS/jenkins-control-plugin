package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.notification.EventLog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.popup.PopupComponent;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Description
 *
 * @author Yuri Novitsky
 */
public class UploadPathToJob extends AnAction implements DumbAware {

    private static final Logger LOG = Logger.getInstance(UploadPathToJob.class.getName());
    private static final String PARAMETER_NAME = "patch.diff";

    private BrowserPanel browserPanel;

    private static final Icon EXECUTE_ICON = GuiUtil.isUnderDarcula() ? GuiUtil.loadIcon("execute_dark.png") : GuiUtil.loadIcon("execute.png");


    public UploadPathToJob(BrowserPanel browserPanel) {
        super("Upload a patch", "Upload a patch to the job", EXECUTE_ICON);
        this.browserPanel = browserPanel;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = ActionUtil.getProject(event);

        String message = "";
        final BrowserPanel browserPanel = BrowserPanel.getInstance(project);

        try {
            Job job = browserPanel.getSelectedJob();

            RequestManager requestManager = browserPanel.getJenkinsManager();

            if (job.hasParameters()) {
                if (job.hasParameter(PARAMETER_NAME)) {

                    JenkinsAppSettings settings = JenkinsAppSettings.getSafeInstance(project);

                    final VirtualFile virtualFile =
                        prepareFile(FileChooser.chooseFile(
                                browserPanel,
                                new FileChooserDescriptor(true, false, false, false, false, false))
                        , settings);

                    if ((null != virtualFile)) {
                        if (virtualFile.exists()) {
                            Map<String, VirtualFile> files = new HashMap<String, VirtualFile>();
                            files.put(PARAMETER_NAME, virtualFile);
                            requestManager.runBuild(job, settings, files);
                            notifyOnGoingMessage(job);
                            browserPanel.loadSelectedJob();
                        } else {
                            message = String.format("File \"%s\" not exists", virtualFile.getPath());
                        }
                    }
                } else {
                    message = String.format("Job \"%s\" should has parameter with name \"%s\"", job.getName(), PARAMETER_NAME);
                }
            } else {
                message = String.format("Job \"%s\" has no parameters", job.getName());
            }

        } catch (Exception e) {
            message = String.format("Build cannot be run: " + e.getMessage());
            e.printStackTrace();
        }

        if (!message.isEmpty()) {
            LOG.info(message);
            browserPanel.notifyErrorJenkinsToolWindow(message);
        }

    }

    private VirtualFile prepareFile(VirtualFile file, JenkinsAppSettings settings) throws IOException {
        if ((null != file) && file.exists()) {
            InputStream stream = file.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader bufferReader = new BufferedReader(streamReader);
            String line = null;
            String suffix = settings.getSuffix();
            StringBuilder builder = new StringBuilder();
            while((line = bufferReader.readLine()) != null) {
                if (line.startsWith("Index: ") && !line.startsWith("Index: " + suffix)) {
                    line = line.replaceFirst("^(Index: )(.+)", "$1" + suffix + "$2");
                }
                if (line.startsWith("--- ") && !line.startsWith("--- " + suffix)) {
                    line = line.replaceFirst("^(--- )(.+)", "$1" + suffix + "$2");
                }
                if (line.startsWith("+++ ") && !line.startsWith("+++ " + suffix)) {
                    line = line.replaceFirst("^(\\+\\+\\+ )(.+)", "$1" + suffix + "$2");
                }
                builder.append(line);
                builder.append("\r\n");
            }
            bufferReader.close();
            streamReader.close();
            stream.close();

            OutputStream outputStream = file.getOutputStream(browserPanel);
            outputStream.write(builder.toString().getBytes(Charset.forName("UTF-8")));
            outputStream.close();
        }

        return file;
    }

    private void notifyOnGoingMessage(Job job) {
        browserPanel.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                job.getName() + " build is on going",
                job.getUrl()));
    }
}
