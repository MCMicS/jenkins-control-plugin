/*
 * Copyright (c) 2013 David Boissier
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

import java.text.SimpleDateFormat;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.SUCCESS;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class BuildTest {

    @Test
    public void isAfter() {
        Build aBuild = build(815);
        Build anotherBuild = build(814);

        assertThat(aBuild.isAfter(anotherBuild), equalTo(TRUE));
        assertThat(anotherBuild.isAfter(aBuild), equalTo(FALSE));
        assertThat(anotherBuild.isAfter(anotherBuild), equalTo(FALSE));
    }


    @Test
    public void isDisplayable() {
        Build currentBuild = build(815);
        Build newBuild = build(815);
        assertFalse(newBuild.isAfter(currentBuild));

        newBuild = build(816);
        assertTrue(newBuild.isAfter(currentBuild));

    }


    private static Build build(int buildNumber) {
        final SimpleDateFormat rssDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return Build.createBuildFromRss("http://jenkinsserver/agf-sql/815",
                buildNumber,
                SUCCESS.getStatus(), true, "2011-03-16T14:28:59Z", "a message", rssDateFormat);
    }
}
