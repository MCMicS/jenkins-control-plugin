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

package org.codinjutsu.tools.jenkins.security;

import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;

public class NoSecurityClientTest extends AbstractSecurityClientTestCase {

    private static final String EXPECTED_XML_CONTENT =
            "<hudson>" +
                    "<assignedLabel></assignedLabel>" +
                    "<mode>NORMAL</mode>" +
                    "<nodeDescription>the master Jenkins node</nodeDescription>" +
                    "<nodeName></nodeName>" +
                    "<numExecutors>2</numExecutors>" +
                    "<job><name>dummyproject</name><url>http://localhost:8080/jenkins/job/dummyproject/</url><color>blue</color></job>" +
                    "<overallLoad></overallLoad>" +
                    "<primaryView><name>All</name><url>http://localhost:8080/jenkins/</url></primaryView>" +
                    "<quietingDown>false</quietingDown>" +
                    "<slaveAgentPort>0</slaveAgentPort>" +
                    "<useCrumbs>false</useCrumbs>" +
                    "<useSecurity>false</useSecurity>" +
                    "<view><name>All</name>" +
                    "<url>http://localhost:8080/jenkins/</url></view>" +
                    "</hudson>";

    @Test
    @Ignore
    public void basicConnection() throws Exception {
        try {
            Thread.sleep(15000);
            NoSecurityClient noSecurityClient = new NoSecurityClient();
            noSecurityClient.connect(new URL(JENKINS_HTTP_URL));
            String responseBody = noSecurityClient.execute(new URL(JENKINS_HTTP_URL + "/api/xml"));
            Assert.assertThat(responseBody, IsEqual.equalTo(EXPECTED_XML_CONTENT));
        } catch (Exception ex) {
            Assert.fail("The connection should not fail : " + ex.getMessage());
        }
    }
}
