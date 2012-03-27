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

package org.codinjutsu.tools.jenkins.logic;

import org.apache.commons.io.IOUtils;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.security.SecurityClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.FAILURE;
import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.SUCCESS;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class JenkinsRequestManagerTest {

    private JenkinsRequestManager requestManager;

    private JenkinsConfiguration configuration;

    @Mock
    private SecurityClient securityClientMock;


    @Test
    public void loadJenkinsWorkSpace() throws Exception {
        Mockito.when(securityClientMock.execute(Mockito.any(URL.class)))
                .thenReturn(IOUtils.toString(JenkinsRequestManagerTest.class.getResourceAsStream("JenkinsRequestManager_loadJenkinsWorkspace.xml")));
        Jenkins jenkins = requestManager.loadJenkinsWorkspace(configuration);

        List<View> actualViews = jenkins.getViews();

        List<View> expectedViews = new LinkedList<View>();
        expectedViews.add(View.createView("Framework", "http://myjenkins/view/Framework/"));
        expectedViews.add(View.createView("Tools", "http://myjenkins/view/Tools/"));
        expectedViews.add(View.createView("Tous", "http://myjenkins/"));

        assertReflectionEquals(expectedViews, actualViews);

        assertReflectionEquals(View.createView("Tous", "http://myjenkins"), jenkins.getPrimaryView());
    }

    @Test
    public void loadJenkinsWorkSpaceWithNestedViews() throws Exception {
        Mockito.when(securityClientMock.execute(Mockito.any(URL.class)))
                .thenReturn(IOUtils.toString(JenkinsRequestManagerTest.class.getResourceAsStream("JenkinsRequestManager_loadJenkinsWorkspaceWithNestedView.xml")));
        Jenkins jenkins = requestManager.loadJenkinsWorkspace(configuration);

        List<View> actualViews = jenkins.getViews();

        List<View> expectedViews = new LinkedList<View>();
        expectedViews.add(View.createView("Framework", "http://myjenkins/view/Framework/"));
        View nestedView = View.createView("NestedView", "http://myjenkins/view/NestedView/");

        nestedView.addSubView(View.createNestedView("FirstSubView", "http://myjenkins/view/NestedView/view/FirstSubView/"));
        nestedView.addSubView(View.createNestedView("SecondSubView", "http://myjenkins/view/NestedView/view/SecondSubView/"));
        expectedViews.add(nestedView);

        expectedViews.add(View.createView("Tous", "http://myjenkins/"));

        assertReflectionEquals(expectedViews, actualViews);

        assertReflectionEquals(View.createView("Tous", "http://myjenkins"), jenkins.getPrimaryView());
    }


    @Test
    public void loadView() throws Exception {
        Mockito.when(securityClientMock.execute(Mockito.any(URL.class)))
                .thenReturn(IOUtils.toString(JenkinsRequestManagerTest.class.getResourceAsStream("JenkinsRequestManager_loadView.xml")));

        List<Job> actualJobs = requestManager.loadJenkinsView("http://myjenkins/");

        List<Job> expectedJobs = new LinkedList<Job>();


        expectedJobs.add(new JobBuilder().job("sql-tools", "blue", "http://myjenkins/job/sql-tools/", "true")
                .lastBuild("http://myjenkins/job/sql-tools/15/", "15", SUCCESS.getStatus(), "false")
                .health("health-80plus", "0 tests en échec sur un total de 24 tests").get());
        expectedJobs.add(new JobBuilder().job("db-utils", "grey", "http://myjenkins/job/db-utils/", "false").get());
        expectedJobs.add(new JobBuilder().job("myapp", "red", "http://myjenkins/job/myapp/", "false")
                .lastBuild("http://myjenkins/job/myapp/12/", "12", FAILURE.getStatus(), "true")
                .health("health-00to19", "24 tests en échec sur un total de 24 tests")
                .parameter("param1", "ChoiceParameterDefinition", "value1", "value1", "value2", "value3")
                .parameter("runIntegrationTest", "BooleanParameterDefinition", null)
                .get());
        expectedJobs.add(new JobBuilder().job("swing-utils", "disabled", "http://myjenkins/job/swing-utils/", "true")
                .lastBuild("http://myjenkins/job/swing-utils/5/", "5", FAILURE.getStatus(), "false")
                .health("health20to39", "0 tests en échec sur un total de 24 tests")
                .parameter("dummyParam", null, null)
                .get());

        assertReflectionEquals(expectedJobs, actualJobs);
    }

    @Test
    public void loadJob() throws Exception {
        Mockito.when(securityClientMock.execute(Mockito.any(URL.class)))
                .thenReturn(IOUtils.toString(JenkinsRequestManagerTest.class.getResourceAsStream("JenkinsRequestManager_loadJob.xml")));

        Job actualJob = requestManager.loadJob("http://ci.jenkins-ci.org/job/config-provider-model/");

        assertReflectionEquals(new JobBuilder()
                .job("config-provider-model", "blue", "http://ci.jenkins-ci.org/job/config-provider-model/", "false")
                .lastBuild("http://ci.jenkins-ci.org/job/config-provider-model/8/", "8", "SUCCESS", "false")
                .health("health-80plus", "0 tests en échec sur un total de 24 tests")
                .get(), actualJob);
    }

    @Test
    public void buildLatestBuildList() throws Exception {
        Mockito.when(securityClientMock.execute(Mockito.any(URL.class)))
                .thenReturn(IOUtils.toString(JenkinsRequestManagerTest.class.getResourceAsStream("JenkinsRss.xml")));

        Map<String, Build> actualJobBuildMap = requestManager.loadJenkinsRssLatestBuilds(configuration);

        Map<String, Build> expectedJobBuildMap = buildLastJobResultMap(new String[][]{
                {"infra_main_svn_to_git", "http://ci.jenkins-ci.org/job/infra_main_svn_to_git/351/", "351", BuildStatusEnum.SUCCESS.getStatus()},
                {"TESTING-HUDSON-7434", "http://ci.jenkins-ci.org/job/TESTING-HUDSON-7434/2/", "2", BuildStatusEnum.FAILURE.getStatus()},
                {"infa_release.rss", "http://ci.jenkins-ci.org/job/infa_release.rss/139/", "139", BuildStatusEnum.SUCCESS.getStatus()},
                {"infra_jenkins-ci.org_webcontents", "http://ci.jenkins-ci.org/job/infra_jenkins-ci.org_webcontents/2/", "2", BuildStatusEnum.SUCCESS.getStatus()},
                {"plugins_subversion", "http://ci.jenkins-ci.org/job/plugins_subversion/58/", "58", BuildStatusEnum.FAILURE.getStatus()},
                {"hudson_metrics_wip", "http://ci.jenkins-ci.org/job/hudson_metrics_wip/6/", "6", BuildStatusEnum.ABORTED.getStatus()},
                {"gerrit_master", "http://ci.jenkins-ci.org/job/gerrit_master/170/", "170", FAILURE.getStatus()},
        });


        assertReflectionEquals(expectedJobBuildMap, actualJobBuildMap);
    }

    private Map<String, Build> buildLastJobResultMap(String[][] datas) {
        Map<String, Build> expectedJobBuildMap = new HashMap<String, Build>();
        for (String[] data : datas) {
            expectedJobBuildMap.put(data[0], Build.createBuild(data[1], data[2], data[3], "false"));
        }
        return expectedJobBuildMap;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configuration = new JenkinsConfiguration();
        requestManager = new JenkinsRequestManager(securityClientMock);
    }

}
