package org.codinjutsu.tools.jenkins.exception;

import org.assertj.core.api.Assertions;
import org.codinjutsu.tools.jenkins.model.Job;
import org.junit.Test;

import static org.junit.Assert.*;

public class NoJobFoundExceptionTest {

    @Test
    public void getMessage() {
        Job job = Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .build();
        final NoJobFoundException noJobFoundException = new NoJobFoundException(job);
        Assertions.assertThat(noJobFoundException.getMessage()).isEqualTo("Could not find Job data for: Job Name [http://url]");
    }

    @Test
    public void getMessageWithDisplayName() {
        Job job = Job.builder().url("http://url").name("Job Name")
                .fullName("Full Job Name")
                .displayName("Display Name")
                .build();
        final NoJobFoundException noJobFoundException = new NoJobFoundException(job);
        Assertions.assertThat(noJobFoundException.getMessage()).isEqualTo("Could not find Job data for: Display Name [http://url]");
    }
}
