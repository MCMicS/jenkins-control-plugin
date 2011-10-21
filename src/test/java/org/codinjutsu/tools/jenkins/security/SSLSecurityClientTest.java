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

import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.security.SslSocketConnector;

import java.net.URL;

public class SSLSecurityClientTest extends AbstractSecurityClientTestCase {

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
    public void withNoCredential() throws Exception {
        SSLSecurityClient sslSecurityClient = new SSLSecurityClient(new EasySSLProtocolSocketFactory());
        try {
            Thread.sleep(15000);
            sslSecurityClient.connect(new URL(JENKINS_HTTPS_URL));
            sslSecurityClient.execute(new URL(JENKINS_HTTPS_URL + "/api/xml"));
            Assert.fail("the connection should fail");
        } catch (Exception e) {
            Assert.assertEquals("Authentication failed", e.getMessage());
        } finally {
            sslSecurityClient.close();
        }

    }


    @Test
    @Ignore
    public void testBadCredentials() throws Exception {
        try {
            Thread.sleep(15000);
            SSLSecurityClient sslSecurityClient = new SSLSecurityClient(new EasySSLProtocolSocketFactory(), "dboissier", "bibi");
            sslSecurityClient.connect(new URL(JENKINS_HTTPS_URL));
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals("Bad Credentials.", e.getMessage());
        }
    }


    @Test
    @Ignore
    public void testRightCredentials() throws Exception {

        try {
            Thread.sleep(15000);
            SSLSecurityClient sslSecurityClient = new SSLSecurityClient(new EasySSLProtocolSocketFactory(), "dboissier", "dboissier");
            sslSecurityClient.connect(new URL(JENKINS_HTTPS_URL));
            String responseBody = sslSecurityClient.execute(new URL(JENKINS_HTTPS_URL + "/api/xml"));
            Assert.assertThat(responseBody, IsEqual.equalTo(EXPECTED_XML_CONTENT));
        } catch (Exception e) {
            Assert.fail("The connection should not fail");
        }

    }


    @Override
    protected Connector createConnector() {
        SslSocketConnector connector = new SslSocketConnector();
        connector.setPort(HTTPS_PORT);
        connector.setMaxIdleTime(60000);
        connector.setKeystore(getClass().getResource("jetty-ssl.keystore").getFile());
        connector.setPassword("jetty6");
        connector.setKeyPassword("jetty6");
        return connector;
    }


    public static void main(String[] args) throws Exception {
        SSLSecurityClientTest securityClientTest = new SSLSecurityClientTest();
        securityClientTest.setup();

        SSLSecurityClient sslSecurityClient = new SSLSecurityClient(new EasySSLProtocolSocketFactory());
        sslSecurityClient.connect(new URL(JENKINS_HTTPS_URL));
        String responseBody = sslSecurityClient.execute(new URL(JENKINS_HTTPS_URL + "/api/xml"));
        System.out.println("responseBody = " + responseBody);

        securityClientTest.tearDown();
    }
}
