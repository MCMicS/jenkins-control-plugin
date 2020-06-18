package org.codinjutsu.tools.jenkins.view;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import icons.JenkinsControlIcons;
import org.codinjutsu.tools.jenkins.JenkinsAppSettings;
import org.codinjutsu.tools.jenkins.logic.UrlBuilder;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.PicoContainer;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.reflect.Whitebox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuildStatusEnumRendererTest {

    //@Spy
    private final BuildStatusRenderer defaultRenderer = new DefaultBuildStatusEnumRenderer();

    private final Project project = mock(Project.class);
    private final JenkinsAppSettings jenkinsAppSettings = new JenkinsAppSettings();

    private BuildStatusEnumRenderer buildStatusEnumRenderer;

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

//        verify(defaultRenderer, times(5)).renderBuildStatus(any());
//        verify(defaultRenderer, never()).renderBuildStatus(BuildStatusEnum.SUCCESS);
//        verify(defaultRenderer, never()).renderBuildStatus(BuildStatusEnum.STABLE);
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

//        verify(defaultRenderer, times(7)).renderBuildStatus(any());
    }

    @Before
    public void setUp() {
        final PicoContainer container = mock(PicoContainer.class);
        when(project.getPicoContainer()).thenReturn(container);
        when(container.getComponentInstance(BuildStatusEnumRenderer.class.getName())).thenReturn(buildStatusEnumRenderer);
        buildStatusEnumRenderer = BuildStatusEnumRenderer.getInstance(project);
        Whitebox.setInternalState(buildStatusEnumRenderer, jenkinsAppSettings);
    }
}
