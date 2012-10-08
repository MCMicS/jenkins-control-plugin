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

package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.logic.JobBuilder;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.model.Jenkins;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.View;
import org.uispec4j.*;

import static java.util.Arrays.asList;

public class JenkinsBrowserPanelTest extends UISpecTestCase {

    private Panel uiSpecBrowserPanel;
    private BrowserPanel browserPanel;

    public void test_displayJenkinsWithTwoJobs() throws Exception {
        ComboBox comboBox = uiSpecBrowserPanel.getComboBox("viewCombo");
        comboBox.contains("Vue 1", "All").check();
        comboBox.selectionEquals("All").check();

        Tree jobTree = getJobTree(uiSpecBrowserPanel);
        jobTree.contentEquals(
                "Jenkins (master)\n" +
                        "  mint #150\n" +
                        "  capri #15 (running) #(bold)\n" +
                        "  bench #10\n").check();
    }

    public void test_sortJobTreeByBuildStatus() throws Exception {
        assertFalse(browserPanel.isSortedByStatus());
        Tree jobTree = getJobTree(uiSpecBrowserPanel);
        jobTree.contentEquals(
                "Jenkins (master)\n" +
                        "  mint #150\n" +
                        "  capri #15 (running) #(bold)\n" +
                        "  bench #10\n").check();

        browserPanel.setSortedByStatus(true);
        jobTree.contentEquals(
                "Jenkins (master)\n" +
                        "  capri #15 (running) #(bold)\n" +
                        "  mint #150\n" +
                        "  bench #10\n").check();

        browserPanel.setSortedByStatus(false);
        jobTree.contentEquals(
                "Jenkins (master)\n" +
                        "  mint #150\n" +
                        "  capri #15 (running) #(bold)\n" +
                        "  bench #10\n").check();
    }

    public void test_displaySearchJobPanel() throws Exception {
        Tree jobTree = getJobTree(uiSpecBrowserPanel);
        jobTree.selectionIsEmpty().check();

        uiSpecBrowserPanel.pressKey(Key.control(Key.F));

        TextBox searchField = uiSpecBrowserPanel.getTextBox("searchField");
        searchField.textIsEmpty().check();

        searchField.setText("capri");
        searchField.pressKey(Key.ENTER);

        jobTree.selectionEquals("capri #15 (running)").check();

//        Section below does not work, perhaps KeyEvent is not properly caugth by the CloseJobSearchPanelAction
//        searchField.pressKey(Key.ESCAPE);
//        uiSpecBrowserPanel.getTextBox("searchField").isVisible().check();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        browserPanel = new BrowserPanel();
        browserPanel.createSearchPanel();
        browserPanel.fillData(createJenkinsWorkspace());
        uiSpecBrowserPanel = new Panel(browserPanel);

    }

    private Jenkins createJenkinsWorkspace() {

        Jenkins jenkins = new Jenkins("(master)", "http://myjenkinsserver");

        Job mintJob = new JobBuilder().job("mint", "blue", "http://myjenkinsserver/mint", "false", "true")
                .lastBuild("http://myjenkinsserver/mint/150", "150", BuildStatusEnum.SUCCESS.getStatus(), "false", "2012-04-02_10-26-29")
                .health("health-80plus", "0 tests en échec sur un total de 89 tests")
                .get();
        Job capriJob = new JobBuilder().job("capri", "red", "http://myjenkinsserver/capri", "false", "true")
                .lastBuild("http://myjenkinsserver/capri/15", "15", BuildStatusEnum.FAILURE.getStatus(), "true", "2012-04-01_10-26-29")
                .health("health-00to19", "15 tests en échec sur un total de 50 tests")
                .get();
        Job benchJob = new JobBuilder().job("bench", "blue", "http://myjenkinsserver/bench", "false", "true")
                .lastBuild("http://myjenkinsserver/bench/10", "10", BuildStatusEnum.SUCCESS.getStatus(), "false", "2012-04-01_10-30-29")
                .health("health-00to19", "0 tests en échec sur un total de 50 tests")
                .get();
        jenkins.setJobs(asList(mintJob, capriJob, benchJob));
        jenkins.setViews(asList(
                View.createView("All", "http://myjenkinsserver/"),
                View.createView("Vue 1", "http://myjenkinsserver/vue1")
        ));

        jenkins.setPrimaryView(View.createView("All", "http://myjenkinsserver/"));

        return jenkins;
    }

    private Tree getJobTree(Panel uiSpecBrowserPanel) {
        Tree jobTree = uiSpecBrowserPanel.getTree("jobTree");
        jobTree.setCellValueConverter(new DefaultTreeCellValueConverter());
        return jobTree;
    }
}