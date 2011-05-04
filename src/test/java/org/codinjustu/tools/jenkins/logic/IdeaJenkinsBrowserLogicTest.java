package org.codinjustu.tools.jenkins.logic;


import org.codinjustu.tools.jenkins.JenkinsConfiguration;
import org.codinjustu.tools.jenkins.model.*;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.uispec4j.ComboBox;
import org.uispec4j.Panel;
import org.uispec4j.Tree;
import org.uispec4j.UISpecTestCase;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class IdeaJenkinsBrowserLogicTest extends UISpecTestCase {

    @Mock
    private JenkinsRequestManager requestManagerMock;

    private IdeaJenkinsBrowserLogic jenkinsBrowserLogic;

    private JenkinsConfiguration configuration;

    private final List<Job> joblist = new ArrayList<Job>();

    @Test
    public void test_displayInitialTreeAndLoadView() throws Exception {
        Mockito.when(requestManagerMock.loadJenkinsWorkspace(configuration))
                .thenReturn(createJenkinsWorkspace());

        Mockito.when(requestManagerMock.loadJenkinsView("http://myjenkinsserver/vue1"))
                .thenReturn(joblist);

        jenkinsBrowserLogic.init();

        Panel panel = new Panel(jenkinsBrowserLogic.getView());

        ComboBox comboBox = panel.getComboBox("viewCombo");
        comboBox.contains("Vue 1", "All").check();
        comboBox.selectionEquals("All").check();

        Tree jobTree = panel.getTree("jobTree");
        jobTree.contentEquals("Jenkins (master)").check();

        comboBox.select("Vue 1");

        panel.getTree("jobTree");
        jobTree.contentEquals(
                "Jenkins (master)\n" +
                        "  mint #150\n" +
                        "  capri #15 (running) #(bold)\n").check();
    }


    @Override
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configuration = new JenkinsConfiguration();
        configuration.setJobRefreshPeriod(60);
        configuration.setServerUrl("http://myjenkinsserver/");
        jenkinsBrowserLogic = new IdeaJenkinsBrowserLogic(configuration, requestManagerMock) {
            @Override
            protected void installRssActions(JPanel rssActionPanel) {

            }


            @Override
            protected void installBrowserActions(JTree jobTree, JPanel panel) {

            }
        };
    }


    private Jenkins createJenkinsWorkspace() {
        Jenkins jenkins = new Jenkins("(master)");

        jenkins.setViews(Arrays.asList(
                View.createView("Vue 1", "http://myjenkinsserver/vue1"),
                View.createView("All", "http://myjenkinsserver/")
        ));

        jenkins.setPrimaryView(View.createView("All", "http://myjenkinsserver/"));

        Job mintJob = Job.createJob("mint", "blue", "http://myjenkinsserver/mint", "false");
        mintJob.setLastBuild(Build.createBuild("http://myjenkinsserver/mint/150",
                "150",
                BuildStatusEnum.SUCCESS.getStatus(),
                "false"));
        Job capriJob = Job.createJob("capri", "red", "http://myjenkinsserver/capri", "false");
        capriJob.setLastBuild(Build.createBuild("http://myjenkinsserver/capri/15",
                "15",
                BuildStatusEnum.FAILURE.getStatus(),
                "true"));

        joblist.add(mintJob);
        joblist.add(capriJob);
        jenkins.setJobs(joblist);

        return jenkins;
    }
}
