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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;


public class BasicSecurityClientTest extends AbstractSecurityClientTestCase {

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
                    "<slaveAgentPort>-1</slaveAgentPort>" +
                    "<useCrumbs>false</useCrumbs>" +
                    "<useSecurity>true</useSecurity>" +
                    "<view><name>All</name>" +
                    "<url>http://localhost:8080/jenkins/</url></view>" +
                    "</hudson>";

    @Override
    protected void doSetup() throws Exception {
        copyInputStream(getClass().getResourceAsStream("jenkins-basic-security-config.xml"),
                new BufferedOutputStream(new FileOutputStream(jenkinsHome + File.separator + "config.xml")));

    }

    @Test
    @Ignore
    public void withNoCredential() throws Exception {
        try {
            Thread.sleep(15000);
            SecurityClient defaultSecurityProvider = new BasicSecurityClient(null, null);
            defaultSecurityProvider.connect(new URL(JENKINS_HTTP_URL));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals("This Jenkins server requires authentication!", e.getMessage());
        }
    }


    @Test
    @Ignore
    public void withBadCredential() throws Exception {
        try {
            Thread.sleep(15000);
            BasicSecurityClient basicSecurityProvider = new BasicSecurityClient("dboissier", "bibi");
            basicSecurityProvider.connect(new URL(JENKINS_HTTP_URL));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals("Bad Credentials.", e.getMessage());
        }
    }


    @Test
    @Ignore
    public void withRightCredential() throws Exception {
        try {
            Thread.sleep(15000);
            BasicSecurityClient basicSecurityProvider = new BasicSecurityClient("dboissier", "dboissier");
            basicSecurityProvider.connect(new URL(JENKINS_HTTP_URL));
            String responseBody = basicSecurityProvider.execute(new URL(JENKINS_HTTP_URL + "/api/xml"));
            Assert.assertEquals(EXPECTED_XML_CONTENT, responseBody);
        } catch (Exception e) {
            Assert.fail("The connection should not fail: " + e.getMessage());
        }
    }
}
