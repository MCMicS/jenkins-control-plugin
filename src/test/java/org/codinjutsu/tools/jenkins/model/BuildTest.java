package org.codinjutsu.tools.jenkins.model;

import org.junit.Test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class BuildTest {

    @Test
    public void test_isAfter() throws Exception {
        Build aBuild = createBuild("815", SUCCESS);
        Build anotherBuild = createBuild("814", FAILURE);

        assertThat(aBuild.isAfter(anotherBuild), equalTo(TRUE));
        assertThat(anotherBuild.isAfter(aBuild), equalTo(FALSE));
        assertThat(anotherBuild.isAfter(anotherBuild), equalTo(FALSE));
    }


    @Test
    public void test_isDisplayable() throws Exception {
        Build currentBuild = createBuild("815", ABORTED);
        Build newBuild = createBuild("815", ABORTED);

        assertThat(newBuild.isDisplayable(currentBuild), equalTo(FALSE));

        currentBuild = createBuild("815", SUCCESS);
        newBuild = createBuild("816", FAILURE);

        assertThat(newBuild.isDisplayable(currentBuild), equalTo(TRUE));

        currentBuild = createBuild("815", SUCCESS);
        newBuild = createBuild("816", ABORTED);

        assertThat(newBuild.isDisplayable(currentBuild), equalTo(TRUE));

        currentBuild = createBuild("815", SUCCESS);
        newBuild = createBuild("816", SUCCESS);

        assertThat(newBuild.isDisplayable(currentBuild), equalTo(FALSE));

        currentBuild = createBuild("815", FAILURE);
        newBuild = createBuild("816", FAILURE);

        assertThat(newBuild.isDisplayable(currentBuild), equalTo(TRUE));

        currentBuild = createBuild("815", FAILURE);
        newBuild = createBuild("816", SUCCESS);

        assertThat(newBuild.isDisplayable(currentBuild), equalTo(TRUE));
    }


    private static Build createBuild(String buildNumber, BuildStatusEnum statusEnum) {
        return Build.createBuild("http://jenkinsserver/agf-sql/815",
                buildNumber,
                statusEnum.getStatus(),
                "true");
    }
}
