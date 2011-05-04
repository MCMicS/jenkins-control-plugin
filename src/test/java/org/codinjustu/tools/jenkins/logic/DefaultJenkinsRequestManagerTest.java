package org.codinjustu.tools.jenkins.logic;

import org.codinjustu.tools.jenkins.JenkinsConfiguration;
import org.codinjustu.tools.jenkins.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.codinjustu.tools.jenkins.model.BuildStatusEnum.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DefaultJenkinsRequestManagerTest {

    private JenkinsRequestManager requestManager;

    private JenkinsConfiguration configuration;

    @Mock
    private UrlBuilder urlBuilderMock;


    @Test
    public void test_loadJenkinsWorkSpace() throws Exception {

        Mockito.when(urlBuilderMock.createJenkinsWorkspaceUrl(Mockito.any(JenkinsConfiguration.class)))
                .thenReturn(DefaultJenkinsRequestManagerTest.class.getResource("JenkinsRequestManager_loadJenkinsWorkSpace.xml"));

        Jenkins jenkins = requestManager.loadJenkinsWorkspace(configuration);

        List<View> actualViews = jenkins.getViews();

        List<View> expectedViews = new LinkedList<View>();
        expectedViews.add(View.createView("Framework", "http://myjenkins/view/Framework/"));
        expectedViews.add(View.createView("Tools", "http://myjenkins/view/Tools/"));
        expectedViews.add(View.createView("Tous", "http://myjenkins/"));

        assertThat(actualViews, equalTo(expectedViews));

        assertThat(jenkins.getPrimaryView(), equalTo(View.createView("Tous", "http://myjenkins")));
    }


    @Test
    public void test_loadView() throws Exception {

        Mockito.when(urlBuilderMock.createViewUrl("http://myjenkins/"))
                .thenReturn(DefaultJenkinsRequestManagerTest.class.getResource(
                        "JenkinsRequestManager_loadView.xml"));

        List<Job> actualJobs = requestManager.loadJenkinsView("http://myjenkins/");

        List<Job> expectedJobs = new LinkedList<Job>();
        Job job = Job.createJob("sql-tools",
                "blue",
                "http://myjenkins/job/sql-tools/", "true");
        job.setLastBuild(Build.createBuild("http://myjenkins/job/sql-tools/15/",
                "15", SUCCESS.getStatus(), "false"));
        expectedJobs.add(job);
        job = Job.createJob("db-utils",
                "grey",
                "http://myjenkins/job/db-utils/", "false");
        expectedJobs.add(job);

        job = Job.createJob("myapp", "red", "http://myjenkins/job/myapp/", "false");
        job.setLastBuild(Build.createBuild("http://myjenkins/job/myapp/12/",
                "12", FAILURE.getStatus(), "true"));
        expectedJobs.add(job);
        job = Job.createJob("swing-utils",
                "disabled",
                "http://myjenkins/job/swing-utils/", "true");
        job.setLastBuild(Build.createBuild("http://myjenkins/job/swing-utils/5/",
                "5", FAILURE.getStatus(), "false"));
        expectedJobs.add(job);

        assertThat(actualJobs, equalTo(expectedJobs));
    }


    @Test
    public void test_buildLatestBuildList() throws Exception {
        Mockito.when(urlBuilderMock.createRssLatestUrl(Mockito.anyString()))
                .thenReturn(DefaultJenkinsRequestManagerTest.class.getResource("JenkinsRss.xml"));

        Map<String, Build> actualJobBuildMap = requestManager.loadJenkinsRssLatestBuilds(configuration);

        Map<String, Build> expectedJobBuildMap = buildLastJobResultMap(new String[][] {
                {"infra_main_svn_to_git", "http://ci.jenkins-ci.org/job/infra_main_svn_to_git/351/", "351", BuildStatusEnum.SUCCESS.getStatus()},
                {"TESTING-HUDSON-7434", "http://ci.jenkins-ci.org/job/TESTING-HUDSON-7434/2/", "2", BuildStatusEnum.FAILURE.getStatus()},
                {"infa_release.rss", "http://ci.jenkins-ci.org/job/infa_release.rss/139/", "139", BuildStatusEnum.SUCCESS.getStatus()},
                {"infra_jenkins-ci.org_webcontents", "http://ci.jenkins-ci.org/job/infra_jenkins-ci.org_webcontents/2/", "2", BuildStatusEnum.SUCCESS.getStatus()},
                {"plugins_subversion", "http://ci.jenkins-ci.org/job/plugins_subversion/58/", "58", BuildStatusEnum.FAILURE.getStatus()},
                {"hudson_metrics_wip", "http://ci.jenkins-ci.org/job/hudson_metrics_wip/6/", "6", BuildStatusEnum.ABORTED.getStatus()},
                {"gerrit_master", "http://ci.jenkins-ci.org/job/gerrit_master/170/", "170", FAILURE.getStatus()},
        });

        assertEquals(expectedJobBuildMap.size(), actualJobBuildMap.size());
        assertThat(actualJobBuildMap, equalTo(expectedJobBuildMap));
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
        requestManager = new DefaultJenkinsRequestManager(urlBuilderMock);
    }
}
