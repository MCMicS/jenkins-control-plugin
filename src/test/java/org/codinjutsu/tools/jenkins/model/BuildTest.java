/*
 * Copyright (c) 2011 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
