package org.codinjutsu.tools.jenkins.view;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import icons.JenkinsControlIcons;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.EnumMap;
import java.util.Map;

@Service
public final class DefaultBuildStatusEnumRenderer implements BuildStatusRenderer {

    private final Map<BuildStatusEnum, Icon> iconByBuildStatus = new EnumMap<>(BuildStatusEnum.class);

    public DefaultBuildStatusEnumRenderer() {
        iconByBuildStatus.put(BuildStatusEnum.SUCCESS, JenkinsControlIcons.Job.BLUE);
        iconByBuildStatus.put(BuildStatusEnum.STABLE, JenkinsControlIcons.Job.BLUE);
        iconByBuildStatus.put(BuildStatusEnum.FAILURE, JenkinsControlIcons.Job.RED);
        iconByBuildStatus.put(BuildStatusEnum.UNSTABLE, JenkinsControlIcons.Job.YELLOW);
        iconByBuildStatus.put(BuildStatusEnum.NULL, JenkinsControlIcons.Job.GREY);
        iconByBuildStatus.put(BuildStatusEnum.ABORTED, JenkinsControlIcons.Job.GREY);
        iconByBuildStatus.put(BuildStatusEnum.FOLDER, AllIcons.Nodes.Folder);
        iconByBuildStatus.put(BuildStatusEnum.RUNNING, JenkinsControlIcons.Job.GREY);
    }

    @NotNull
    public static DefaultBuildStatusEnumRenderer getInstance(@NotNull Project project) {
        DefaultBuildStatusEnumRenderer renderer = ServiceManager.getService(project, DefaultBuildStatusEnumRenderer.class);
        return renderer == null ? new DefaultBuildStatusEnumRenderer() : renderer;
    }

    @NotNull
    @Override
    public Icon renderBuildStatus(@NotNull BuildStatusEnum buildStatus) {
        return iconByBuildStatus.getOrDefault(buildStatus, JenkinsControlIcons.Job.GREY);
    }
}
