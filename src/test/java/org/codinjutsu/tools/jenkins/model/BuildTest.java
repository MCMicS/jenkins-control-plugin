/*
 * Copyright (c) 2012 David Boissier
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

import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BuildTest {

    @Test
    public void test_isAfter() throws Exception {
        Build aBuild = build("815");
        Build anotherBuild = build("814");

        assertThat(aBuild.isAfter(anotherBuild), equalTo(TRUE));
        assertThat(anotherBuild.isAfter(aBuild), equalTo(FALSE));
        assertThat(anotherBuild.isAfter(anotherBuild), equalTo(FALSE));
    }


    @Test
    public void test_isDisplayable() throws Exception {
        Build currentBuild = build("815");
        Build newBuild = build("815");
        assertFalse(newBuild.isAfter(currentBuild));

        newBuild = build("816");
        assertTrue(newBuild.isAfter(currentBuild));

    }


    private static Build build(String buildNumber) {
        return Build.createBuildFromRss("http://jenkinsserver/agf-sql/815",
                buildNumber,
                SUCCESS.getStatus(),
                "true", "2011-03-16T14:28:59Z", "a message");
    }



    public static Map<String, Build> buildLastJobResultMap(String[][] datas) {
        Map<String, Build> expectedJobBuildMap = new HashMap<String, Build>();
        for (String[] data : datas) {
            expectedJobBuildMap.put(data[0], Build.createBuildFromRss(data[1], data[2], data[3], "false", data[4], data[5]));
        }
        return expectedJobBuildMap;
    }
}
