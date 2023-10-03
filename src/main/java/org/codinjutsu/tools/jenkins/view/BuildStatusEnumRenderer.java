package org.codinjutsu.tools.jenkins.view;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import icons.JenkinsControlIcons;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.model.Color;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;

@Service(Service.Level.PROJECT)
public final class BuildStatusEnumRenderer implements BuildStatusRenderer {

    private final Map<BuildStatusEnum, Icon> iconByBuildStatus = new EnumMap<>(BuildStatusEnum.class);
    @NotNull
    private final JenkinsAppSettings jenkinsAppSettings;

    private BuildStatusEnumRenderer(@NotNull Project project) {
        this.jenkinsAppSettings = JenkinsAppSettings.getSafeInstance(project);

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
    public static BuildStatusEnumRenderer getInstance(@NotNull Project project) {
        BuildStatusEnumRenderer renderer = project.getService(BuildStatusEnumRenderer.class);
        return renderer == null ? new BuildStatusEnumRenderer(project) : renderer;
    }

    @NotNull
    @Override
    public Icon renderBuildStatus(@NotNull BuildStatusEnum buildStatus) {
        if (buildStatus.getColor() == Color.BLUE && jenkinsAppSettings.isUseGreenColor()) {
            return JenkinsControlIcons.Job.GREEN;
        }
        return iconByBuildStatus.getOrDefault(buildStatus, JenkinsControlIcons.Job.GREY);
    }
}
