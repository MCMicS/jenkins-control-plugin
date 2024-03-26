package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.view.extension.ViewTestResults;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShowBuildResultsAsJUnitViewAction extends AnAction {

    private static final Icon ICON = AllIcons.Actions.GroupByTestProduction;
    @NotNull
    private final LogToolWindow logToolWindow;

    public ShowBuildResultsAsJUnitViewAction(@NotNull LogToolWindow logToolWindow) {
        super("Show test results", getDefaultDescription(), ICON);
        this.logToolWindow = logToolWindow;
    }

    @NotNull
    @Nls(capitalization = Nls.Capitalization.Sentence)
    private static String getDefaultDescription() {
        return "Show test results in separate view";
    }

    @NotNull
    private static Predicate<ViewTestResults> canHandleJob(@NotNull Build build) {
        return viewTestResult -> viewTestResult.canHandle(build);
    }

    @NotNull
    private static Stream<ViewTestResults> extensionsForBuild(@NotNull Build build) {
        return ViewTestResults.EP_NAME.getExtensionList().stream().filter(canHandleJob(build));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        //final Job job = browserPanel.getSelectedJob()
        final var buildToShowTests = getBuild();
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (buildToShowTests.isPresent() && project != null) {
            final var build = buildToShowTests.get();
            extensionsForBuild(build).forEach(handler -> handler.handle(project, build));
        }
    }

    @Override
    public void update(AnActionEvent event) {
        //final Optional<Job> selectedJob = Optional.ofNullable(browserPanel.getSelectedJob());
        final var selectedJob = getBuild();
        final Collection<ViewTestResults> usableExtensions = selectedJob
                .map(ShowBuildResultsAsJUnitViewAction::extensionsForBuild)
                .map(s -> s.collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        final String description = getDescription(usableExtensions);
        event.getPresentation().setDescription(description);
        event.getPresentation().setVisible(ViewTestResults.EP_NAME.hasAnyExtensions());
        event.getPresentation().setEnabled(!usableExtensions.isEmpty());
    }

    private Optional<Build> getBuild() {
        return logToolWindow.getBuild();
    }

    @NotNull
    @Nls(capitalization = Nls.Capitalization.Sentence)
    private String getDescription(@NotNull Collection<ViewTestResults> extensions) {
        if (extensions.isEmpty()) {
            return getDefaultDescription();
        } else {
            return extensions.stream().map(ViewTestResults::getDescription)
                    .filter(Objects::nonNull).collect(Collectors.joining(", "));
        }
    }
}
