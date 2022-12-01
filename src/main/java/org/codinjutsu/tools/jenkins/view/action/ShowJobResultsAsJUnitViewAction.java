package org.codinjutsu.tools.jenkins.view.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
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

public class ShowJobResultsAsJUnitViewAction extends AnAction {

    private static final Icon ICON = AllIcons.Actions.GroupByTestProduction;
    @NotNull
    private final BrowserPanel browserPanel;

    public ShowJobResultsAsJUnitViewAction(@NotNull BrowserPanel browserPanel) {
        super("Show test results", getDefaultDescription(), ICON);
        this.browserPanel = browserPanel;
    }

    @NotNull
    @Nls(capitalization = Nls.Capitalization.Sentence)
    private static String getDefaultDescription() {
        return "Show test results in separate view";
    }

    @NotNull
    private static Predicate<ViewTestResults> canHandleJob(@NotNull Job job) {
        return viewTestResult -> viewTestResult.canHandle(job);
    }

    @NotNull
    private static Stream<ViewTestResults> extensionsForJob(@NotNull Job job) {
        return ViewTestResults.EP_NAME.getExtensionList().stream().filter(canHandleJob(job));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Job job = browserPanel.getSelectedJob();
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if (job != null && project != null) {
            extensionsForJob(job).forEach(handler -> handler.handle(project, job));
        }
    }

    @Override
    public void update(AnActionEvent event) {
        final Optional<Job> selectedJob = Optional.ofNullable(browserPanel.getSelectedJob());
        final Collection<ViewTestResults> usableExtensions = selectedJob
                .map(ShowJobResultsAsJUnitViewAction::extensionsForJob)
                .map(s -> s.collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        final String description = getDescription(usableExtensions);
        event.getPresentation().setDescription(description);
        event.getPresentation().setVisible(ViewTestResults.EP_NAME.hasAnyExtensions());
        event.getPresentation().setEnabled(!usableExtensions.isEmpty());
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
