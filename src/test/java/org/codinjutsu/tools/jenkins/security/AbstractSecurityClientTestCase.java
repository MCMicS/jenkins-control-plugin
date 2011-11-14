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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class AbstractSecurityClientTestCase {

    protected static final String HOST = "localhost";

    protected static final int STANDARD_PORT = 8080;
    protected static final int HTTPS_PORT = 8443;


    protected static final String JENKINS_HTTP_URL = "http://" + HOST + ":" + STANDARD_PORT + "/jenkins";

    protected static final String JENKINS_HTTPS_URL = "https://" + HOST + ":" + HTTPS_PORT + "/jenkins";

    private static Server server;

    protected String jenkinsHome;


    private String getJenkinsRepository() {
        return "jenkins-no-security";
    }


    protected void doSetup() throws Exception {

    }

    @Before
    public void setup() throws Exception {
        unflatZipFile(new File(AbstractSecurityClientTestCase.class.getResource(getJenkinsRepository() + ".zip").getFile()));
        jenkinsHome = getClass().getResource(getJenkinsRepository()).getFile();
        System.out.println("jenkinsHome = " + jenkinsHome);
        System.setProperty("JENKINS_HOME", jenkinsHome);
        doSetup();
        server = new Server();

        Connector connector = createConnector();
        server.addConnector(connector);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setWar(getClass().getResource("jenkins.war").getFile());
        webAppContext.setContextPath("/jenkins");
        //expanded war or path of war file

        server.addHandler(webAppContext);
        server.setStopAtShutdown(true);

        server.start();

    }


    protected Connector createConnector() {
        Connector connector = new SelectChannelConnector();
        connector.setPort(STANDARD_PORT);
        connector.setHost(HOST);
        return connector;
    }


    @After
    public void tearDown() throws Exception {
        server.stop();

        File jenkinsRepository = new File(getClass().getResource(getJenkinsRepository()).getFile());
        Assert.assertTrue(jenkinsRepository.exists());
        Assert.assertTrue(jenkinsRepository.isDirectory());
        deleteDirectory(jenkinsRepository);
    }


    private void unflatZipFile(File file) throws Exception {
        String parentDir = file.getParent();
        ZipFile zipFile = new ZipFile(file);
        Enumeration entries = zipFile.entries();


        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            String targetFile = parentDir + File.separator + entry.getName();
            if (entry.isDirectory()) {
                // Assume directories are stored parents first then children.
                // This is not robust, just for demonstration purposes.
                (new File(targetFile)).mkdir();
                continue;
            }

            copyInputStream(zipFile.getInputStream(entry),
                    new BufferedOutputStream(new FileOutputStream(targetFile)));
        }

        zipFile.close();
    }


    protected static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        in.close();
        out.close();
    }


    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDirectory(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
