package org.codinjutsu.tools.jenkins.logic;

import com.intellij.notification.*;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service
public class JenkinsNotifier {

    private final NotificationGroup jenkinsGroup;
    @Nullable
    private final Project project;

    JenkinsNotifier(@Nullable Project project) {
        this.project = project;
        jenkinsGroup = NotificationGroupManager.getInstance().getNotificationGroup("Jenkins Notification");
    }

    @NotNull
    public static JenkinsNotifier getInstance(@NotNull Project project) {
        JenkinsNotifier jenkinsNotifier = ServiceManager.getService(project, JenkinsNotifier.class);
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
    public Notification notify(@Nullable Project project, String content, NotificationType notificationType) {
        final Notification notification = jenkinsGroup.createNotification(content, notificationType);
        notification.setListener(NotificationListener.URL_OPENING_LISTENER);
        notification.notify(project);
        return notification;
    }
}
