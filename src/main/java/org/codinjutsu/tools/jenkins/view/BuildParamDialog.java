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

package org.codinjutsu.tools.jenkins.view;

import com.intellij.icons.AllIcons;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JobTracker;
import org.codinjutsu.tools.jenkins.TraceableBuildJob;
import org.codinjutsu.tools.jenkins.TraceableBuildJobFactory;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderer;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.codinjutsu.tools.jenkins.view.parameter.JobParameterComponent;
import org.codinjutsu.tools.jenkins.view.util.SpringUtilities;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BuildParamDialog extends JDialog {
    private static final Logger logger = Logger.getLogger(BuildParamDialog.class);
    private final Job job;
    private final JenkinsAppSettings configuration;
    private final RequestManager requestManager;
    private final RunBuildCallback runBuildCallback;
    private final Collection<JobParameterComponent> inputFields = new LinkedHashSet<>();
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel contentPanel;

    BuildParamDialog(Job job, JenkinsAppSettings configuration, RequestManager requestManager, RunBuildCallback runBuildCallback) {
        this.job = job;
        this.configuration = configuration;
        this.requestManager = requestManager;
        this.runBuildCallback = runBuildCallback;

        contentPanel.setName("contentPanel");

        addParameterInputs();
        setTitle("This build requires parameters");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        registerListeners();
    }

    public static void showDialog(final Job job, final JenkinsAppSettings configuration, final RequestManager requestManager,
                                  final RunBuildCallback runBuildCallback) {
        SwingUtilities.invokeLater(() -> {
            BuildParamDialog dialog = new BuildParamDialog(job, configuration, requestManager, runBuildCallback);
            dialog.setLocationRelativeTo(null);
            dialog.setSize(dialog.getPreferredSize());
            //dialog.setMinimumSize(new Dimension(300, 200));
            dialog.setMaximumSize(new Dimension(800, 600));
            dialog.pack();
            dialog.setVisible(true);
        });
    }

    @NotNull
    private static JLabel appendColonIfMissing(@NotNull JLabel label) {
        if (!label.getText().endsWith(":")) {
            label.setText(label.getText() + ":");
        }
        return label;
    }

    @NotNull
    private static JLabel setJLabelStyles(@NotNull JLabel label) {
        label.setHorizontalAlignment(SwingConstants.TRAILING);
        //label.setVerticalAlignment(SwingConstants.TOP);
        return label;
    }

    @NotNull
    private static Function<JLabel, JLabel> setJLabelStyles(@NotNull JobParameterComponent jobParameterComponent) {
        return label -> {
            label.setLabelFor(jobParameterComponent.getViewElement());
            setJLabelStyles(label);
            return label;
        };
    }

    private void addParameterInputs() {
        contentPanel.setLayout(new SpringLayout());
        List<JobParameter> parameters = job.getParameters();

        final AtomicInteger rows = new AtomicInteger(0);
        for (JobParameter jobParameter : parameters) {
            final JobParameterRenderer jobParameterRenderer = JobParameterRenderer.findRenderer(jobParameter)
                    .orElseGet(ErrorRenderer::new);
            final JobParameterComponent jobParameterComponent = jobParameterRenderer.render(jobParameter);
            if (jobParameterComponent.isVisible()) {
                rows.incrementAndGet();
                jobParameterComponent.getViewElement().setName(jobParameter.getName());

                final JLabel label = jobParameterRenderer.createLabel(jobParameter)
                        .map(setJLabelStyles(jobParameterComponent))
                        .map(BuildParamDialog::appendColonIfMissing)
                        .orElseGet(JLabel::new);
                contentPanel.add(label);
                contentPanel.add(jobParameterComponent.getViewElement());

                final String description = jobParameter.getDescription();
                if (StringUtils.isNotEmpty(description)) {
                    JLabel placeHolder = new JLabel("", SwingConstants.CENTER);
                    contentPanel.add(placeHolder);
                    contentPanel.add(new JLabel(description));
                    rows.incrementAndGet();
                }

                inputFields.add(jobParameterComponent);
            }
        }

        final int columns = 2;
        final int initial = 6;
        final int padding = 6;
        SpringUtilities.makeCompactGrid(contentPanel,
                rows.get(), columns,
                initial, initial,
                padding, padding);

        if (hasError()) {
            buttonOK.setEnabled(false);
        }
    }

    private boolean hasError() {
        return inputFields.stream().anyMatch(JobParameterComponent::hasError);
    }

    private void registerListeners() {
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        final Map<String, String> paramValueMap = getParamValueMap();

        new SwingWorker<Void, Void>() { //FIXME don't use swing worker
                @Override
                protected Void doInBackground() throws Exception {
                TraceableBuildJob buildJob = TraceableBuildJobFactory.newBuildJob(job, configuration, paramValueMap, requestManager);
                JobTracker.getInstance().registerJob(buildJob);
                requestManager.runParameterizedBuild(job, configuration, paramValueMap);
                return null;
            }

            @Override
            protected void done() {
                dispose();
                try {
                    get();
                    runBuildCallback.notifyOnOk(job);
                } catch (InterruptedException e) {
                    logger.log(Level.WARN, "Exception occured while...", e);
                } catch (ExecutionException e) {
                    runBuildCallback.notifyOnError(job, e);
                    logger.log(Level.WARN, "Exception occured while trying to invoke build", e);
                }

            }
        }.execute();
    }

    private void onCancel() {
        dispose();
    }

    @NotNull
    private Map<String, String> getParamValueMap() {
        final HashMap<String, String> valueByNameMap = new HashMap<>();
        for (JobParameterComponent jobParameterComponent : inputFields) {
            final JobParameter jobParameter = jobParameterComponent.getJobParameter();
            jobParameterComponent.ifHasValue(value -> valueByNameMap.put(jobParameter.getName(), value));
        }
        return valueByNameMap;
    }

    public interface RunBuildCallback {

        void notifyOnOk(Job job);

        void notifyOnError(Job job, Exception ex);
    }

    public class ErrorRenderer implements JobParameterRenderer {

        @NotNull
        @Override
        public JobParameterComponent render(@NotNull JobParameter jobParameter) {
            return JobParameterRenderers.createErrorLabel(jobParameter);
        }

        @Override
        public boolean isForJobParameter(@NotNull JobParameter jobParameter) {
            return true;
        }
    }
}
