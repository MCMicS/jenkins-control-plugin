package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.offbytwo.jenkins.helper.BuildConsoleStreamListener;
import lombok.Value;
import org.codinjutsu.tools.jenkins.exception.JenkinsPluginRuntimeException;
import org.codinjutsu.tools.jenkins.logic.RequestManagerInterface;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildType;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class LogToolWindow {

    static final String TOOL_WINDOW_ID = "JenkinsLogs";

    private final Project project;

    public LogToolWindow(Project project) {
        this.project = project;
    }

    public void showLog(BuildType buildType, Job job, BrowserPanel browserPanel) {
        final String jobName = job.getNameToRenderSingleJob();
        final String logTabTitle = getTabTitle(buildType, job);

        final ShowLogConsoleView showLogConsoleView = createConsoleView(project, browserPanel);

        final ConsoleView consoleView = showLogConsoleView.getConsoleView();
        final LogProcessHandler processHandler = new LogProcessHandler();
        consoleView.attachToProcess(processHandler);
        processHandler.startNotify();
        showInToolWindow(showLogConsoleView, logTabTitle);

        new Task.Backgroundable(project, "Loading log for " + jobName, false) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    final RequestManagerInterface requestManager = browserPanel.getJenkinsManager();
                    requestManager.loadConsoleTextFor(job, buildType, processHandler);
                } catch (JenkinsPluginRuntimeException e) {
                    browserPanel.notifyErrorJenkinsToolWindow(e.getMessage());
                }
            }
        }.queue();
    }

    @NotNull
    private static ShowLogConsoleView createConsoleView(Project project, BrowserPanel browserPanelForAction) {
        final TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
        builder.setViewer(true);
        final ConsoleView consoleView = builder.getConsole();

        final DefaultActionGroup toolbarActions = new DefaultActionGroup();
        // panel creation for call to #createConsoleActions needed
        final JComponent panel = createConsolePanel(consoleView, toolbarActions);
        toolbarActions.addAll(consoleView.createConsoleActions());
        toolbarActions.addAction(new ShowJobResultsAsJUnitViewAction(browserPanelForAction));
        panel.updateUI();
        return new ShowLogConsoleView(consoleView, panel);
    }

    private static JComponent createConsolePanel(ConsoleView view, ActionGroup actions) {
        final ActionToolbar actionToolbar = createToolbar(actions);
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(view.getComponent(), BorderLayout.CENTER);
        panel.add(actionToolbar.getComponent(), BorderLayout.WEST);
        actionToolbar.setTargetComponent(panel);
        return panel;
    }

    private static ActionToolbar createToolbar(ActionGroup actions) {
        return ActionManager.getInstance().createActionToolbar("JenkinsLogWindow", actions, false);
    }

    private void showInToolWindow(ShowLogConsoleView showLogConsoleView, String tabName) {
        getToolWindow().ifPresent(toolWindow -> GuiUtil.showInToolWindow(toolWindow, showLogConsoleView.getPanel(),
                showLogConsoleView.getConsoleView(), tabName));
    }

    @NotNull
    private Optional<ToolWindow> getToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        return Optional.ofNullable(toolWindowManager.getToolWindow(TOOL_WINDOW_ID));
    }

    @NotNull
    static String getTabTitle(BuildType buildType, Job job) {
        final String jobName = job.getNameToRenderSingleJob();
        final String buildInfo;
        switch (buildType) {
            case LAST_SUCCESSFUL:
                buildInfo = "(Last Successful)";
                break;
            case LAST_FAILED:
                buildInfo = "(Last Failed)";
                break;
            case LAST://Fallthrough
            default:
                buildInfo = Optional.ofNullable(job.getLastBuild()).map(Build::getDisplayNumber).orElse("(Last)");
        }
        return String.format("%s %s", jobName, buildInfo);
    }

    static class LogProcessHandler extends NopProcessHandler implements BuildConsoleStreamListener {

        @Override
        public void onData(String consoleContent) {
            notifyTextAvailable(consoleContent, ProcessOutputType.STDOUT);
        }

        @Override
        public void finished() {
            notifyProcessDetached();
        }
    }

    @Value
    static class ShowLogConsoleView {

        @NotNull ConsoleView consoleView;
        @NotNull JComponent panel;

    }

    static final class ShowLogRunDescriptor extends RunContentDescriptor {

        public ShowLogRunDescriptor(@Nullable ExecutionConsole executionConsole,
                                    @Nullable ProcessHandler processHandler,
                                    @NotNull JComponent component,
                                    @NlsContexts.TabTitle String displayName) {
            super(executionConsole, processHandler, component, displayName);
        }

        @Override
        public @Nullable String getContentToolWindowId() {
            return super.getContentToolWindowId();
        }

        public void init() {
            setActivateToolWindowWhenAdded(true);
            setAutoFocusContent(true);
        }
    }
}
