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
import org.codinjutsu.tools.jenkins.view.JenkinsBrowserPanel;
import org.codinjutsu.tools.jenkins.view.JobSearchComponent;
import org.codinjutsu.tools.jenkins.view.RssLatestBuildPanel;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.uispec4j.*;

import javax.swing.*;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;


public class JenkinsBrowserLogicTest extends UISpecTestCase {

    @Mock
    private JenkinsRequestManager requestManagerMock;

    private JenkinsConfiguration configuration;
    private JenkinsBrowserLogic jenkinsBrowserLogic;

    private Panel uiSpecBrowserPanel;
    private Panel uiSpecRssPanel;

    public void test_displayInitialTreeAndLoadView() throws Exception {

        ComboBox comboBox = uiSpecBrowserPanel.getComboBox("viewCombo");
        comboBox.contains("Vue 1", "All").check();
        comboBox.selectionEquals("All").check();

        Tree jobTree = getJobTree(uiSpecBrowserPanel);
        jobTree.contentEquals(
                "Jenkins (master)\n" +
                        "  mint #150\n" +
                        "  capri #15 (running) #(bold)\n").check();

        comboBox.select("Vue 1");

        Thread.sleep(100);//waiting for the swing thread finished

        getJobTree(uiSpecBrowserPanel);
        jobTree.contentEquals(
                "Jenkins (master)\n" +
                        "  capri #15 (running) #(bold)\n").check();
    }

    public void test_displaySearchJobPanel() throws Exception {
        Tree jobTree = getJobTree(uiSpecBrowserPanel);
        jobTree.selectionIsEmpty().check();

        Thread.sleep(100);//waiting for the swing thread finished

        uiSpecBrowserPanel.pressKey(Key.control(Key.F));

        TextBox searchField = uiSpecBrowserPanel.getTextBox("searchField");
        searchField.textIsEmpty().check();

        searchField.setText("capri");
        searchField.pressKey(Key.ENTER);

        jobTree.selectionEquals("capri #15 (running)").check();

        //Section below does not work, perhaps KeyEvent is not properly caugth by the CloseJobSearchPanelAction
        searchField.pressKey(Key.ESCAPE);
        uiSpecBrowserPanel.getTextBox("searchField").isVisible().check();
    }

    public void test_RssReader() throws Exception {
        TextBox rssContent = uiSpecRssPanel.getTextBox("rssContent");

        rssContent.textIsEmpty().check();

        when(requestManagerMock.loadJenkinsRssLatestBuilds(configuration)).thenReturn(BuildTest.buildLastJobResultMap(new String[][]{
                {"infra_main_svn_to_git", "http://ci.jenkins-ci.org/job/infra_main_svn_to_git/352/", "352", BuildStatusEnum.FAILURE.getStatus(), "2012-03-03T17:01:51Z", "infra_main_svn_to_git #351 (broken)"}, // new build but fail
                {"infra_jenkins-ci.org_webcontents", "http://ci.jenkins-ci.org/job/infra_jenkins-ci.org_webcontents/2/", "2", BuildStatusEnum.SUCCESS.getStatus(), "2011-02-02T00:49:58Z", "infra_jenkins-ci.org_webcontents #2 (back to normal)"}, // unchanged
                {"infa_release.rss", "http://ci.jenkins-ci.org/job/infa_release.rss/140/", "140", BuildStatusEnum.SUCCESS.getStatus(), "2012-03-03T20:30:51Z", "infa_release.rss #140 (back to normal)"}, // new build but success
                {"TESTING-HUDSON-7434", "http://ci.jenkins-ci.org/job/TESTING-HUDSON-7434/3/", "3", BuildStatusEnum.FAILURE.getStatus(), "2012-03-03T05:27:56Z", "TESTING-HUDSON-7434 #3 (broken for a long time)"}, //new build but still fail
        }));
        jenkinsBrowserLogic.loadLatestBuilds();
        assertTrue(rssContent.textContains(
                "<html>\n" +
                        "  <head>\n" +
                        "  </head>\n" +
                        "  <body>" +
                        "05:27:56 <font color=\"red\"><a href=\"http://ci.jenkins-ci.org/job/TESTING-HUDSON-7434/3/\">TESTING-HUDSON-7434 #3 (broken for a long time)</a></font><br>\n" +
                        "17:01:51 <font color=\"red\"><a href=\"http://ci.jenkins-ci.org/job/infra_main_svn_to_git/352/\">infra_main_svn_to_git #351 (broken)</a></font><br>\n" +
                        "20:30:51 <font color=\"blue\">infa_release.rss #140 (back to normal)</font><br></body>\n" +
                        "</html>")
        );

        jenkinsBrowserLogic.cleanRssEntries();
        rssContent.textIsEmpty().check();
    }


    private Tree getJobTree(Panel panel) {
        Tree jobTree = panel.getTree("jobTree");
        jobTree.setCellValueConverter(new DefaultTreeCellValueConverter());
        return jobTree;
    }


    @Override
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        configuration = new JenkinsConfiguration();
        configuration.setJobRefreshPeriod(60);
        configuration.setServerUrl("http://myjenkinsserver/");
        jenkinsBrowserLogic = new JenkinsBrowserLogic(configuration, requestManagerMock, new JenkinsBrowserPanel(), new RssLatestBuildPanel(), JenkinsBrowserLogic.RssBuildStatusCallback.NULL, JenkinsBrowserLogic.JobViewCallback.NULL) {
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

        Job mintJob = new JobBuilder().job("mint", "blue", "http://myjenkinsserver/mint", "false")
                .lastBuild("http://myjenkinsserver/mint/150", "150", BuildStatusEnum.SUCCESS.getStatus(), "false", "2012-04-02_10-26-29")
                .health("health-80plus", "0 tests en échec sur un total de 89 tests")
                .get();
        Job capriJob = new JobBuilder().job("capri", "red", "http://myjenkinsserver/capri", "false")
                .lastBuild("http://myjenkinsserver/capri/15", "15", BuildStatusEnum.FAILURE.getStatus(), "true", "2012-04-01_10-26-29")
                .health("health-00to19", "15 tests en échec sur un total de 50 tests")
                .get();


        when(requestManagerMock.loadJenkinsWorkspace(configuration)).thenReturn(createJenkinsWorkspace());

        when(requestManagerMock.loadJenkinsView("http://myjenkinsserver/")).thenReturn(asList(mintJob, capriJob));

        when(requestManagerMock.loadJenkinsView("http://myjenkinsserver/vue1")).thenReturn(asList(capriJob));

        when(requestManagerMock.loadJenkinsRssLatestBuilds(configuration)).thenReturn(BuildTest.buildLastJobResultMap(new String[][]{
                {"infra_main_svn_to_git", "http://ci.jenkins-ci.org/job/infra_main_svn_to_git/351/", "351", BuildStatusEnum.SUCCESS.getStatus(), "2010-11-21T17:01:51Z", "infra_main_svn_to_git #351 (stable)"},
                {"infra_jenkins-ci.org_webcontents", "http://ci.jenkins-ci.org/job/infra_jenkins-ci.org_webcontents/2/", "2", BuildStatusEnum.SUCCESS.getStatus(), "2011-02-02T00:49:58Z", "infra_jenkins-ci.org_webcontents #2 (back to normal)"},
                {"infa_release.rss", "http://ci.jenkins-ci.org/job/infa_release.rss/139/", "139", BuildStatusEnum.FAILURE.getStatus(), "2011-03-16T20:30:51Z", "infa_release.rss #139 (broken)"},
                {"TESTING-HUDSON-7434", "http://ci.jenkins-ci.org/job/TESTING-HUDSON-7434/2/", "2", BuildStatusEnum.FAILURE.getStatus(), "2011-03-02T05:27:56Z", "TESTING-HUDSON-7434 #2 (broken for a long time)"},
        }));

        jenkinsBrowserLogic.init();
        Thread.sleep(500);
        uiSpecBrowserPanel = new Panel(jenkinsBrowserLogic.getJenkinsBrowserPanel());
        uiSpecRssPanel = new Panel(jenkinsBrowserLogic.getRssLatestJobPanel());
    }

    private Jenkins createJenkinsWorkspace() {
        Jenkins jenkins = new Jenkins("(master)");

        jenkins.setViews(asList(
                View.createView("Vue 1", "http://myjenkinsserver/vue1"),
                View.createView("All", "http://myjenkinsserver/")
        ));

        jenkins.setPrimaryView(View.createView("All", "http://myjenkinsserver/"));


        return jenkins;
    }
}
