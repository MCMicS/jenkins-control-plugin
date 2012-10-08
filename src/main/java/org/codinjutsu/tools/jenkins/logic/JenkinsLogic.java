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

import com.intellij.openapi.Disposable;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.view.BrowserPanel;
import org.codinjutsu.tools.jenkins.view.RssLatestBuildPanel;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class JenkinsLogic implements Disposable {

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);

    private final BrowserLogic browserLogic;
    private final RssLogic rssLogic;

    public JenkinsLogic(JenkinsConfiguration configuration, RequestManager requestManager, BrowserPanel browserPanel, RssLatestBuildPanel rssLatestJobPanel,
                        RssLogic.BuildStatusListener buildStatusListener, BrowserLogic.JobLoadListener jobLoadListener) {
        browserLogic = new BrowserLogic(configuration, requestManager, browserPanel, jobLoadListener);
        rssLogic = new RssLogic(configuration, requestManager, rssLatestJobPanel, buildStatusListener);
    }

    public void init() {
        browserLogic.init();
        rssLogic.init();

        browserLogic.initScheduledJobs(scheduledThreadPoolExecutor);
        rssLogic.initScheduledJobs(scheduledThreadPoolExecutor);
    }

    public void dispose() {
       browserLogic.dispose();
       rssLogic.dispose();

       scheduledThreadPoolExecutor.shutdown();
    }

    public void reloadConfiguration() {
        browserLogic.reloadConfiguration();
        rssLogic.reloadConfiguration();
    }


}
