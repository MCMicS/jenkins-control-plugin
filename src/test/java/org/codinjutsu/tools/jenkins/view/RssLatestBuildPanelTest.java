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

import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.model.BuildTest;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.UISpecTestCase;

public class RssLatestBuildPanelTest extends UISpecTestCase {


    public void disabled_test_addFinishedBuild() throws Exception {
        RssLatestBuildPanel rssLatestBuildPanel = new RssLatestBuildPanel();
        Panel uiSpecPanel = new Panel(rssLatestBuildPanel);

        TextBox rssContent = uiSpecPanel.getTextBox("rssContent");
        rssContent.textIsEmpty().check();

        rssLatestBuildPanel.addFinishedBuild(BuildTest.buildLastJobResultMap(new String[][]{
                {"infa_release.rss", "http://ci.jenkins-ci.org/job/infa_release.rss/140/", "140", BuildStatusEnum.SUCCESS.getStatus(), "2012-03-03T20:30:51Z", "infa_release.rss #140 (back to normal)"}, // new build but success
                {"infra_main_svn_to_git", "http://ci.jenkins-ci.org/job/infra_main_svn_to_git/352/", "352", BuildStatusEnum.FAILURE.getStatus(), "2012-03-03T17:01:51Z", "infra_main_svn_to_git #351 (broken)"}, // new build but fail
                {"TESTING-HUDSON-7434", "http://ci.jenkins-ci.org/job/TESTING-HUDSON-7434/3/", "3", BuildStatusEnum.FAILURE.getStatus(), "2012-03-03T05:27:56Z", "TESTING-HUDSON-7434 #3 (broken for a long time)"}, //new build but still fail
        }));

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
    }


    public void test_cleanRssEntries() throws Exception {
        RssLatestBuildPanel rssLatestBuildPanel = new RssLatestBuildPanel();
        Panel uiSpecPanel = new Panel(rssLatestBuildPanel);

        TextBox rssContent = uiSpecPanel.getTextBox("rssContent");
        rssContent.textIsEmpty().check();

        rssLatestBuildPanel.addFinishedBuild(BuildTest.buildLastJobResultMap(new String[][]{
                {"infra_main_svn_to_git", "http://ci.jenkins-ci.org/job/infra_main_svn_to_git/352/", "352", BuildStatusEnum.FAILURE.getStatus(), "2012-03-03T17:01:51Z", "infra_main_svn_to_git #351 (broken)"}, // new build but fail
        }));

        rssLatestBuildPanel.cleanRssEntries();

        rssContent.textIsEmpty().check();
    }
}
