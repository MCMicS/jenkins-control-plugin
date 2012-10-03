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

package org.codinjutsu.tools.jenkins.logic;


import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.*;
import org.codinjutsu.tools.jenkins.security.AuthenticationException;
import org.codinjutsu.tools.jenkins.security.SecurityMode;
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserPanel;
import org.codinjutsu.tools.jenkins.view.JobSearchComponent;
import org.codinjutsu.tools.jenkins.view.RssLatestBuildPanel;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uispec4j.*;

import javax.swing.*;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


public class BrowserLogicTest extends UISpecTestCase {

    @Mock
    private JenkinsRequestManager requestManagerMock;

    private JenkinsConfiguration configuration;
    private BrowserLogic browserLogic;

    private Panel uiSpecBrowserPanel;
    private MyJobLoadListener jobViewCallback;


    public void test_displayWithEmptyServerUrl() throws Exception {
        init("");

        Tree jobTree = getJobTree(uiSpecBrowserPanel);
        jobTree.contentEquals("Jenkins (Missing configuration. Check Jenkins Plugin Settings.)\n").check();
    }

    public void test_displayWithDummyServerUrl() throws Exception {
        init(JenkinsConfiguration.DUMMY_JENKINS_SERVER_URL);

        Tree jobTree = getJobTree(uiSpecBrowserPanel);
        jobTree.contentEquals("Jenkins (Missing configuration. Check Jenkins Plugin Settings.)\n").check();
    }

    public void test_displayWithAuthenticationFailure() throws Exception {
        //todo need to refactor this
        createConfiguration("http://anyserver");
        createLogic();
        doThrow(new AuthenticationException("fail")).when(requestManagerMock).authenticate(anyString(), any(SecurityMode.class), anyString(), anyString(), anyString());

        this.browserLogic.init();
        Thread.sleep(500);

        initUI();

        Tree jobTree = getJobTree(uiSpecBrowserPanel);
        jobTree.contentEquals("Jenkins (Unable to connect. Check Jenkins Plugin Settings.)\n").check();
    }


    public void disable_test_displayInitialTreeAndLoadView() throws Exception {
        init("http://myjenkinsserver/");

        ComboBox comboBox = uiSpecBrowserPanel.getComboBox("viewCombo");
        comboBox.contains("Vue 1", "All").check();
        comboBox.selectionEquals("All").check();

        Tree jobTree = getJobTree(uiSpecBrowserPanel);
        jobTree.contentEquals(
                "Jenkins (master)\n" +
                        "  mint #150\n" +
                        "  capri #15 (running) #(bold)\n").check();

        assertEquals(1, jobViewCallback.buildStatusAggregator.getNbSucceededBuilds());
        assertEquals(0, jobViewCallback.buildStatusAggregator.getNbUnstableBuilds());
        assertEquals(1, jobViewCallback.buildStatusAggregator.getNbBrokenBuilds());
        assertEquals(0, jobViewCallback.buildStatusAggregator.getNbAbortedBuilds());

        comboBox.select("Vue 1");

        getJobTree(uiSpecBrowserPanel);
        jobTree.contentEquals(
                "Jenkins (master)\n" +
                        "  capri #15 (running) #(bold)\n").check();

        assertEquals(0, jobViewCallback.buildStatusAggregator.getNbSucceededBuilds());
        assertEquals(0, jobViewCallback.buildStatusAggregator.getNbUnstableBuilds());
        assertEquals(1, jobViewCallback.buildStatusAggregator.getNbBrokenBuilds());
        assertEquals(0, jobViewCallback.buildStatusAggregator.getNbAbortedBuilds());
    }


    private Tree getJobTree(Panel panel) {
        Tree jobTree = panel.getTree("jobTree");
        jobTree.setCellValueConverter(new DefaultTreeCellValueConverter());
        return jobTree;
    }


    @Override
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    private void init(String serverUrl) throws Exception {
        createConfiguration(serverUrl);

        initLogic();
        initUI();
    }

    private void initLogic() throws Exception {
        createLogic();

        prepareMock();

        browserLogic.init();
        Thread.sleep(800);
    }

    private void createLogic() {
        jobViewCallback = new MyJobLoadListener();
        browserLogic = new BrowserLogic(configuration, requestManagerMock, new JenkinsBrowserPanel(), new RssLatestBuildPanel(), BrowserLogic.BuildStatusListener.NULL, jobViewCallback) {
            @Override
            protected void installRssActions(JPanel rssActionPanel) {
            }

            @Override
            protected void installBrowserActions(JTree jobTree, JPanel panel) {
            }

            @Override
            protected void installSearchActions(JobSearchComponent searchComponent) {
            }
        };
    }

    private void prepareMock() throws Exception {
        Job mintJob = new JobBuilder().job("mint", "blue", "http://myjenkinsserver/mint", "false", "true")
                .lastBuild("http://myjenkinsserver/mint/150", "150", BuildStatusEnum.SUCCESS.getStatus(), "false", "2012-04-02_10-26-29")
                .health("health-80plus", "0 tests en échec sur un total de 89 tests")
                .get();
        Job capriJob = new JobBuilder().job("capri", "red", "http://myjenkinsserver/capri", "false", "true")
                .lastBuild("http://myjenkinsserver/capri/15", "15", BuildStatusEnum.FAILURE.getStatus(), "true", "2012-04-01_10-26-29")
                .health("health-00to19", "15 tests en échec sur un total de 50 tests")
                .get();

        doNothing().when(requestManagerMock).authenticate(anyString(), any(SecurityMode.class), anyString(), anyString(), anyString());

        when(requestManagerMock.loadJenkinsWorkspace(configuration)).thenReturn(createJenkinsWorkspace());

        when(requestManagerMock.loadJenkinsView("http://myjenkinsserver/")).thenReturn(asList(mintJob, capriJob));

        when(requestManagerMock.loadJenkinsView("http://myjenkinsserver/vue1")).thenReturn(asList(capriJob));

        when(requestManagerMock.loadJenkinsRssLatestBuilds(configuration)).thenReturn(BuildTest.buildLastJobResultMap(new String[][]{
                {"infra_main_svn_to_git", "http://ci.jenkins-ci.org/job/infra_main_svn_to_git/351/", "351", BuildStatusEnum.SUCCESS.getStatus(), "2010-11-21T17:01:51Z", "infra_main_svn_to_git #351 (stable)"},
                {"infra_jenkins-ci.org_webcontents", "http://ci.jenkins-ci.org/job/infra_jenkins-ci.org_webcontents/2/", "2", BuildStatusEnum.SUCCESS.getStatus(), "2011-02-02T00:49:58Z", "infra_jenkins-ci.org_webcontents #2 (back to normal)"},
                {"infa_release.rss", "http://ci.jenkins-ci.org/job/infa_release.rss/139/", "139", BuildStatusEnum.FAILURE.getStatus(), "2011-03-16T20:30:51Z", "infa_release.rss #139 (broken)"},
                {"TESTING-HUDSON-7434", "http://ci.jenkins-ci.org/job/TESTING-HUDSON-7434/2/", "2", BuildStatusEnum.FAILURE.getStatus(), "2011-03-02T05:27:56Z", "TESTING-HUDSON-7434 #2 (broken for a long time)"},
        }));
    }

    private void initUI() {
        uiSpecBrowserPanel = new Panel(browserLogic.getJenkinsBrowserPanel());
    }

    private void createConfiguration(String serverUrl) {
        configuration = new JenkinsConfiguration();
        configuration.setJobRefreshPeriod(60);
        configuration.setServerUrl(serverUrl);
    }

    private Jenkins createJenkinsWorkspace() {
        Jenkins jenkins = new Jenkins("(master)", "http://myjenkinsserver");

        jenkins.setViews(asList(
                View.createView("Vue 1", "http://myjenkinsserver/vue1"),
                View.createView("All", "http://myjenkinsserver/")
        ));

        jenkins.setPrimaryView(View.createView("All", "http://myjenkinsserver/"));


        return jenkins;
    }

    private static class MyJobLoadListener implements BrowserLogic.JobLoadListener {

        private BuildStatusAggregator buildStatusAggregator;

        @Override
        public void afterLoadingJobs(BuildStatusAggregator buildStatusAggregator) {
            this.buildStatusAggregator = buildStatusAggregator;
        }
    }
}
