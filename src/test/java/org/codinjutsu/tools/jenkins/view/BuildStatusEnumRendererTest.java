package org.codinjutsu.tools.jenkins.view;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import icons.JenkinsControlIcons;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.util.MockUtil;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class BuildStatusEnumRendererTest {

    private final JenkinsAppSettings jenkinsAppSettings = new JenkinsAppSettings();
    private final Project project = MockUtil.mockProject(jenkinsAppSettings);

    private final BuildStatusEnumRenderer buildStatusEnumRenderer = BuildStatusEnumRenderer.getInstance(project);

    @Test
    public void renderBuildStatusWithGreenSuccessfulColor() {
        jenkinsAppSettings.setUseGreenColor(false);

        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.SUCCESS)).isEqualTo(JenkinsControlIcons.Job.BLUE);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.STABLE)).isEqualTo(JenkinsControlIcons.Job.BLUE);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.FAILURE)).isEqualTo(JenkinsControlIcons.Job.RED);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.UNSTABLE)).isEqualTo(JenkinsControlIcons.Job.YELLOW);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.NULL)).isEqualTo(JenkinsControlIcons.Job.GREY);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.ABORTED)).isEqualTo(JenkinsControlIcons.Job.GREY);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.FOLDER)).isEqualTo(AllIcons.Nodes.Folder);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.RUNNING)).isEqualTo(JenkinsControlIcons.Job.GREY);
    }

    @Test
    public void renderBuildStatusWithBlueSuccessfulColor() {
        jenkinsAppSettings.setUseGreenColor(true);

        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.SUCCESS)).isEqualTo(JenkinsControlIcons.Job.GREEN);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.STABLE)).isEqualTo(JenkinsControlIcons.Job.GREEN);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.FAILURE)).isEqualTo(JenkinsControlIcons.Job.RED);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.UNSTABLE)).isEqualTo(JenkinsControlIcons.Job.YELLOW);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.NULL)).isEqualTo(JenkinsControlIcons.Job.GREY);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.ABORTED)).isEqualTo(JenkinsControlIcons.Job.GREY);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.FOLDER)).isEqualTo(AllIcons.Nodes.Folder);
        assertThat(buildStatusEnumRenderer.renderBuildStatus(BuildStatusEnum.RUNNING)).isEqualTo(JenkinsControlIcons.Job.GREY);
    }
}
