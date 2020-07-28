package org.codinjutsu.tools.jenkins.model;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class JobTest {

    @Test
    public void getNameToRenderSingleJob() {
        final Job withName = Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .build();
        final Job withDisplayName = Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .displayName("Job Display Name")
                .build();
        final Job withFullDisplayName = Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .displayName("Job Display Name")
                .fullDisplayName("Full Job Display Name")
                .build();
        final Job withEmptyFullDisplayName = Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .displayName("Job Display Name")
                .fullDisplayName("")
                .build();

        Assertions.assertThat(withName.getNameToRenderSingleJob()).isEqualTo(withName.getName());
        Assertions.assertThat(withDisplayName.getNameToRenderSingleJob()).isEqualTo(withDisplayName.getDisplayName());
        Assertions.assertThat(withFullDisplayName.getNameToRenderSingleJob()).isEqualTo(withFullDisplayName.getFullDisplayName());
        Assertions.assertThat(withEmptyFullDisplayName.getNameToRenderSingleJob()).isEqualTo(withEmptyFullDisplayName.getDisplayName());
    }

    @Test
    public void preferDisplayName() {
        final Job withName = Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .build();
        final Job withDisplayName = Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .displayName("Job Display Name")
                .build();
        final Job withFullDisplayName = Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .displayName("Job Display Name")
                .fullDisplayName("Full Job Display Name")
                .build();
        final Job withEmptyFullDisplayName = Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .displayName("Job Display Name")
                .fullDisplayName("")
                .build();

        Assertions.assertThat(withName.preferDisplayName()).isEqualTo(withName.getName());
        Assertions.assertThat(withDisplayName.preferDisplayName()).isEqualTo(withDisplayName.getDisplayName());
        Assertions.assertThat(withFullDisplayName.preferDisplayName()).isEqualTo(withFullDisplayName.getDisplayName());
        Assertions.assertThat(withEmptyFullDisplayName.preferDisplayName()).isEqualTo(withEmptyFullDisplayName.getDisplayName());
    }
}
