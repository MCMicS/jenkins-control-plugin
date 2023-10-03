package org.codinjutsu.tools.jenkins.logic;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.DataManager;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Service(Service.Level.PROJECT)
public final class JenkinsNotifier {

    private static final Logger LOG = Logger.getInstance(JenkinsNotifier.class);

    private final NotificationGroup jenkinsGroup;
    @Nullable
    private final Project project;

    JenkinsNotifier(@Nullable Project project) {
        this.project = project;
        jenkinsGroup = NotificationGroupManager.getInstance().getNotificationGroup("Jenkins Notification");
    }

    @NotNull
    public static JenkinsNotifier getInstance(@NotNull Project project) {
        JenkinsNotifier jenkinsNotifier = project.getService(JenkinsNotifier.class);
        return jenkinsNotifier == null ? new JenkinsNotifier(project) : jenkinsNotifier;
    }

    public static void notifyForCurrentContext(String content, NotificationType notificationType) {
        notifyForCurrentContext(jenkinsNotifier -> jenkinsNotifier.notify(content, notificationType));
    }

    public static void notifyForCurrentContext(Consumer<JenkinsNotifier> jenkinsNotifier) {
        GuiUtil.runInSwingThread(() -> DataManager.getInstance().getDataContextFromFocusAsync()
                .then(CommonDataKeys.PROJECT::getData)
                .then(JenkinsNotifier::getInstance)
                .onSuccess(jenkinsNotifier)
                .onError(LOG::error));
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
