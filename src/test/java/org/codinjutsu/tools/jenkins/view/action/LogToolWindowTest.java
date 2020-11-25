package org.codinjutsu.tools.jenkins.view.action;

import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.model.BuildType;
import org.codinjutsu.tools.jenkins.model.Job;
import org.junit.Test;

import java.util.Date;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.*;

public class LogToolWindowTest {

    private final Job job = createJobWithAllBuilds();

    private static Job createJobWithAllBuilds() {
        return Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .displayName("Display Name")
                .availableBuildTypes(EnumSet.allOf(BuildType.class))
                .lastBuild(createLastBuild())
                .build();
    }

    private static Build createLastBuild() {
        return createLastBuild(false);
    }

    private static Build createLastBuild(boolean building) {
        return Build.builder()
                .url("http://jenkins.example.org/job/single_build/42/")
                .number(42)
                .status(BuildStatusEnum.SUCCESS)
                .building(building)
                .timestamp(new Date(0L))
                .duration(0L)
                .building(false)
                .message("Single Build #42 (stable)")
                .build();
    }

    @Test
    public void getTabTitleForMissingLastBuild() {
        job.setLastBuild(null);
        final String lastLog = LogToolWindow.getTabTitle(BuildType.LAST, job);
        assertThat(lastLog).isEqualTo("Display Name (Last)");
    }

    @Test
    public void getTabTitleForLastBuild() {
        final String lastLog = LogToolWindow.getTabTitle(BuildType.LAST, job);
        assertThat(lastLog).isEqualTo("Display Name #42");
    }

    @Test
    public void getTabTitleForLastBuildRunning() {
        job.setLastBuild(createLastBuild(true));
        final String lastLog = LogToolWindow.getTabTitle(BuildType.LAST, job);
        assertThat(lastLog).isEqualTo("Display Name #42");
    }

    @Test
    public void getTabTitleLastSuccessfulBuild() {
        final String lastLog = LogToolWindow.getTabTitle(BuildType.LAST_SUCCESSFUL, job);
        assertThat(lastLog).isEqualTo("Display Name (Last Successful)");
    }

    @Test
    public void getTabTitleLastFailedBuild() {
        final String lastLog = LogToolWindow.getTabTitle(BuildType.LAST_FAILED, job);
        assertThat(lastLog).isEqualTo("Display Name (Last Failed)");
    }
}
