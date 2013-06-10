/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.HtmlUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Description
 *
 * @author Yuri Novitsky
 */
public class UploadPatchToJob extends AnAction implements DumbAware {

    public static final String PARAMETER_NAME = "patch.diff";
    public static final String SUFFIX_JOB_NAME_MACROS = "$JobName$";

    private static final Logger LOG = Logger.getInstance(UploadPatchToJob.class.getName());
    private BrowserPanel browserPanel;

    private static final Icon EXECUTE_ICON = GuiUtil.isUnderDarcula() ? GuiUtil.loadIcon("execute_dark.png") : GuiUtil.loadIcon("execute.png");


    public UploadPatchToJob(BrowserPanel browserPanel) {
        super("Upload Patch and build", "Upload Patch to the job and build", EXECUTE_ICON);
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
                        prepareFile(browserPanel, FileChooser.chooseFile(
                                browserPanel,
                                new FileChooserDescriptor(true, false, false, false, false, false))
                        , settings, job);

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

    public static VirtualFile prepareFile(BrowserPanel browserPanel, VirtualFile file, JenkinsAppSettings settings, Job job) throws IOException {
        if ((null != file) && file.exists()) {
            InputStream stream = file.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader bufferReader = new BufferedReader(streamReader);
            String line = null;
            String suffix = settings.getSuffix();
            suffix = suffix.replace(SUFFIX_JOB_NAME_MACROS, job.getName());
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

    @Override
    public void update(AnActionEvent event) {
        Job selectedJob = browserPanel.getSelectedJob();
        event.getPresentation().setEnabled(selectedJob != null && selectedJob.hasParameters() && selectedJob.hasParameter(PARAMETER_NAME));
    }

    private void notifyOnGoingMessage(Job job) {
        browserPanel.notifyInfoJenkinsToolWindow(HtmlUtil.createHtmlLinkMessage(
                job.getName() + " build is on going",
                job.getUrl()));
    }
}
