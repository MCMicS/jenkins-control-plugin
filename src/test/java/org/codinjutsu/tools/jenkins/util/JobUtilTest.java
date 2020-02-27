package org.codinjutsu.tools.jenkins.util;

import org.codinjutsu.tools.jenkins.JenkinsSettings;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobType;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class JobUtilTest {

    private static final String FAVORITE_NAME = "Favorite Test";
    private static final String FAVORITE_URL = "http://url-to-favorite.com";
    private static final JenkinsSettings.FavoriteJob FAVORITE_JOB = new JenkinsSettings.FavoriteJob(FAVORITE_NAME, FAVORITE_URL);

    @Test
    public void isFavoriteJobWithDifferentName() {
        String favoriteJobName = "Sample Name";
        Job job = createDefaultJob().name(favoriteJobName).build();
        assertThat(JobUtil.isFavoriteJob(job, FAVORITE_JOB)).isFalse();

        job = createDefaultJob().name(FAVORITE_NAME).build();
        assertThat(JobUtil.isFavoriteJob(job, FAVORITE_JOB)).isFalse();

        job = createDefaultJob().displayName(FAVORITE_NAME).build();
        assertThat(JobUtil.isFavoriteJob(job, FAVORITE_JOB)).isFalse();
    }

    @Test
    public void isFavoriteJobWithSameFullName() {
        assertThat(JobUtil.isFavoriteJob(createDefaultJob().fullName(FAVORITE_NAME).build(), FAVORITE_JOB)).isTrue();
    }


    @Test
    public void isFavoriteJobWithSameUrl() {
        assertThat(JobUtil.isFavoriteJob(createDefaultJob().url(FAVORITE_URL).build(), FAVORITE_JOB)).isTrue();
    }

    @Test
    public void createNameForFavorite() {
        assertThat(JobUtil.createNameForFavorite(createDefaultJob().build())).isEqualTo("FullName");
        assertThat(JobUtil.createNameForFavorite(createDefaultJob().fullName(FAVORITE_NAME).build()))
                .isEqualTo(FAVORITE_NAME);
    }

    @NotNull
    private Job.JobBuilder createDefaultJob() {
        return Job.builder().name("Test").jobType(JobType.JOB).displayName("DisplayName")
                .fullName("FullName").url("http://url-to-test.com").inQueue(false).buildable(true);
    }
}
