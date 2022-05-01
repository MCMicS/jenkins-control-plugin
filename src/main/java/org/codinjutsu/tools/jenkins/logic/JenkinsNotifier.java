package org.codinjutsu.tools.jenkins.logic;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.*;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.JenkinsToolWindowFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service
public final class JenkinsNotifier {

    private final NotificationGroup jenkinsGroup;
    @Nullable
    private final Project project;

    JenkinsNotifier(@Nullable Project project) {
        this.project = project;
        jenkinsGroup = NotificationGroup.toolWindowGroup("Jenkins Notification",
                JenkinsToolWindowFactory.JENKINS_BROWSER);
    }

    @NotNull
    public static JenkinsNotifier getInstance(@NotNull Project project) {
        JenkinsNotifier jenkinsNotifier = project.getService(JenkinsNotifier.class);
        return jenkinsNotifier == null ? new JenkinsNotifier(project) : jenkinsNotifier;
    }

    @NotNull
    public Notification error(String content) {
        return error(project, content);
    }

    @NotNull
    public Notification error(@Nullable Project project, String content) {
        return notify(project, content, NotificationType.ERROR);
    }

    @NotNull
    public Notification warning(String content) {
        return notify(project, content, NotificationType.WARNING);
    }

    @NotNull
    public Notification warning(@Nullable Project project, String content) {
        return notify(project, content, NotificationType.WARNING);
    }

    @NotNull
    public Notification notify(String content, NotificationType notificationType) {
        return notify(project, content, notificationType);
    }

    @NotNull
    public Notification notify(String content, String urlToOpen, NotificationType notificationType) {
        return notify(project, content, urlToOpen, notificationType);
    }

    @NotNull
    public Notification notify(@Nullable Project project, String content, NotificationType notificationType) {
        final Notification notification = createNotification(content, notificationType);
        notification.notify(project);
        return notification;
    }

    @NotNull
    public Notification notify(@Nullable Project project, String content, String urlToOpen,
                               NotificationType notificationType) {
        final Notification notification = createNotification(content, notificationType);
        final NotificationAction openInBrowser = NotificationAction.createSimple("Open in browser",
                () -> BrowserUtil.browse(urlToOpen));
        notification.addAction(openInBrowser);
        notification.notify(project);
        return notification;
    }

    @NotNull
    private Notification createNotification(String content, NotificationType notificationType) {
        return jenkinsGroup.createNotification(content, notificationType);
    }
}
