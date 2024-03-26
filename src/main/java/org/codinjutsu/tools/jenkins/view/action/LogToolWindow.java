package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputType;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import lombok.Value;
import org.codinjutsu.tools.jenkins.logic.JenkinsBackgroundTaskFactory;
import org.codinjutsu.tools.jenkins.logic.RequestManager;
import org.codinjutsu.tools.jenkins.logic.RequestManagerInterface;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildType;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.function.Supplier;

public class LogToolWindow {

    static final String TOOL_WINDOW_ID = "JenkinsLogs";

    private final Project project;
    private @Nullable Build build;

    public LogToolWindow(Project project) {
        this.project = project;
    }

    public Optional<Build> getBuild() {
        return Optional.ofNullable(build);
    }

    @NotNull
    private static ShowLogConsoleView createConsoleView(Project project, LogToolWindow logToolWindow) {
        final TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
        builder.setViewer(true);
        final ConsoleView consoleView = builder.getConsole();

        final DefaultActionGroup toolbarActions = new DefaultActionGroup();
        // panel creation for call to #createConsoleActions needed
        final JComponent panel = createConsolePanel(consoleView, toolbarActions);
        toolbarActions.addAll(consoleView.createConsoleActions());
        toolbarActions.addAction(new ShowBuildResultsAsJUnitViewAction(logToolWindow));
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

    public void showLog(Build build) {
        showLog(build::getNameToRender,
                (requestManager, processHandler) -> requestManager.loadConsoleTextFor(build, processHandler));
    }

    public void showLog(BuildType buildType, Job job) {
        showLog(job::getNameToRenderSingleJob, () -> getTabTitle(buildType, job),
                (requestManager, processHandler) -> requestManager.loadConsoleTextFor(job, buildType, processHandler));
    }

    private void showLog(Supplier<String> tabTitle,
                         @NotNull ShowLogHandler showLogHandler) {
        showLog(tabTitle, tabTitle, showLogHandler);
    }

    private void showLog(@NotNull Supplier<String> jobTitle,
                         @NotNull Supplier<String> tabTitle, @NotNull ShowLogHandler showLogHandler) {
        final var showLogConsoleView = createConsoleView(project, LogToolWindow.this);
        final var consoleView = showLogConsoleView.getConsoleView();
        final var processHandler = new LogProcessHandler();
        consoleView.attachToProcess(processHandler);
        processHandler.startNotify();
        showInToolWindow(showLogConsoleView, tabTitle.get());
        final var canBeCancelled = true;
        JenkinsBackgroundTaskFactory.getInstance(project).createBackgroundTask("Loading log for " + jobTitle.get(),
                canBeCancelled, requestManager -> showLogHandler.loadLog(requestManager, processHandler)).queue();
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

    @FunctionalInterface
    public interface ShowLogHandler {
        void loadLog(@NotNull RequestManagerInterface requestManager, @NotNull LogProcessHandler processHandler);
    }

    private class LogProcessHandler extends NopProcessHandler implements RequestManager.BuildLogConsoleStreamListener {

        @Override
        public void onData(String consoleContent) {
            notifyTextAvailable(consoleContent, ProcessOutputType.STDOUT);
        }

        @Override
        public void finished() {
            notifyProcessDetached();
        }

        @Override
        public void forBuild(Build build) {
            LogToolWindow.this.build = build;
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
