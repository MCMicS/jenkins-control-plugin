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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.DimensionService;
import com.intellij.openapi.util.text.StringUtil;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.JobTracker;
import org.codinjutsu.tools.jenkins.TraceableBuildJob;
import org.codinjutsu.tools.jenkins.TraceableBuildJobFactory;
import org.codinjutsu.tools.jenkins.logic.RequestManagerInterface;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.model.ProjectJob;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderer;
import org.codinjutsu.tools.jenkins.view.extension.JobParameterRenderers;
import org.codinjutsu.tools.jenkins.view.parameter.JobParameterComponent;
import org.codinjutsu.tools.jenkins.view.util.SpringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class BuildParamDialog extends DialogWrapper {
    private static final String LAST_SIZE = "jenkins.build.parameter";
    private static final Logger logger = Logger.getInstance(BuildParamDialog.class);
    private final Job job;
    private final @NotNull Project project;
    private final JenkinsAppSettings configuration;
    private final RequestManagerInterface requestManager;
    private final RunBuildCallback runBuildCallback;
    private final Collection<JobParameterComponent<?>> inputFields = new LinkedHashSet<>();
    private JPanel contentPane;
    private JPanel contentPanel;

    BuildParamDialog(@NotNull Project project, Job job, JenkinsAppSettings configuration,
                     RequestManagerInterface requestManager, RunBuildCallback runBuildCallback) {
        super(project);
        this.project = project;
        init();
        setTitle("This build requires parameters");
        this.job = job;
        this.configuration = configuration;
        this.requestManager = requestManager;
        this.runBuildCallback = runBuildCallback;

        contentPanel.setName("contentPanel");

        addParameterInputs();
        setModal(true);
        setAutoAdjustable(false);
    }

    public static void showDialog(@NotNull Project project, final Job job, final JenkinsAppSettings configuration,
                                  final RequestManagerInterface requestManager,
                                  final RunBuildCallback runBuildCallback) {
        ApplicationManager.getApplication().invokeLater(() -> {
            final BuildParamDialog dialog = new BuildParamDialog(project, job, configuration, requestManager, runBuildCallback);
            dialog.pack();
            dialog.restoreLastWidth();
            if (dialog.showAndGet()) {
                dialog.onOK();
            }
            dialog.saveLastSize();
        }, ModalityState.NON_MODAL);
    }

    private void restoreLastWidth() {
        final var height = getWindow().getHeight();
        final var storedSize = DimensionService.getInstance().getSize(LAST_SIZE, project);
        if (storedSize != null) {
            final var preferredSize = new Dimension();
            preferredSize.setSize(storedSize.getWidth(), height);
            getWindow().setPreferredSize(preferredSize);
            pack();
        }
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
        return label;
    }

    @NotNull
    private static Function<JLabel, JLabel> setJLabelStyles(@NotNull JobParameterComponent<?> jobParameterComponent) {
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
            final ProjectJob projectJob = ProjectJob.builder().project(project).job(job).build();
            final JobParameterComponent<?> jobParameterComponent = jobParameterRenderer.render(jobParameter, projectJob);

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
                if (StringUtil.isNotEmpty(description)) {
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

        getOKAction().setEnabled(!hasError());
    }

    private boolean hasError() {
        return inputFields.stream().anyMatch(JobParameterComponent::hasError);
    }

    private void onOK() {
        new RunBuild(project, job, configuration, getParamValueMap(), requestManager, runBuildCallback).queue();
    }

    private void saveLastSize() {
        DimensionService.getInstance().setSize(LAST_SIZE, getSize(), project);
    }

    @NotNull
    private Map<String, ?> getParamValueMap() {
        final HashMap<String, Object> valueByNameMap = new HashMap<>();
        for (JobParameterComponent<?> jobParameterComponent : inputFields) {
            final JobParameter jobParameter = jobParameterComponent.getJobParameter();
            jobParameterComponent.ifHasValue(value -> valueByNameMap.put(jobParameter.getName(), value));
        }
        return valueByNameMap;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public interface RunBuildCallback {

        void notifyOnOk(Job job);

        void notifyOnError(Job job, Throwable ex);
    }

    public class ErrorRenderer implements JobParameterRenderer {

        @NotNull
        @Override
        public JobParameterComponent<String> render(@NotNull JobParameter jobParameter, @Nullable ProjectJob projectJob) {
            return JobParameterRenderers.createErrorLabel(jobParameter);
        }

        @Override
        public boolean isForJobParameter(@NotNull JobParameter jobParameter) {
            return true;
        }
    }

    private static class RunBuild extends Task.Backgroundable {

        private final Job job;
        private final JenkinsAppSettings configuration;
        private final Map<String, ?> paramValueMap;
        private final RequestManagerInterface requestManager;
        private final RunBuildCallback runBuildCallback;

        RunBuild(Project project, Job job, JenkinsAppSettings configuration, Map<String, ?> paramValueMap,
                 RequestManagerInterface requestManager, RunBuildCallback runBuildCallback) {
            super(project, "Running Jenkins build", false);
            this.job = job;
            this.configuration = configuration;
            this.paramValueMap = paramValueMap;
            this.requestManager = requestManager;
            this.runBuildCallback = runBuildCallback;
        }

        @Override
        public void onSuccess() {
            super.onSuccess();
            runBuildCallback.notifyOnOk(job);
        }

        @Override
        public void onThrowable(@NotNull Throwable error) {
            logger.warn("Exception occured while trying to invoke build", error);
            runBuildCallback.notifyOnError(job, error);
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            indicator.setIndeterminate(true);

            TraceableBuildJob buildJob = TraceableBuildJobFactory.newBuildJob(job, configuration, paramValueMap, requestManager);
            JobTracker.getInstance().registerJob(buildJob);
            buildJob.run();
        }
    }
}
