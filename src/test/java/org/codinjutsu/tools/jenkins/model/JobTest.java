package org.codinjutsu.tools.jenkins.model;

import org.codinjutsu.tools.jenkins.logic.JobBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JobTest {
    @Test
    @Ignore
    public void testUpdateContentWithJobInQueue() throws Exception {

        Job buildInQueue = new JobBuilder().job("myJob", "null", "http://jenkins/jobs/myJob", "true").get();

        Job refreshedBuildInQueue = new JobBuilder().job("myJob", "null", "http://jenkins/jobs/myJob", "true").get();
        Assert.assertFalse(buildInQueue.updateContentWith(refreshedBuildInQueue));

        Job refreshedBuildNotInQueueButRunning = new JobBuilder().job("myJob", "null", "http://jenkins/jobs/myJob", "false")
                .lastBuild("http://jenkins/jobs/myJob/1", "1", "null", "true")
                .get();
//        Assert.assertFalse(buildInQueue.updateContentWith(refreshedBuildNotInQueueButRunning));

        buildInQueue.setLastBuild(Build.createBuild("http://jenkins/jobs/myJob/1", "1", "null", "true"));

        Job refreshedBuildRunning = new JobBuilder().job("myJob", "null", "http://jenkins/jobs/myJob", "false")
                .lastBuild("http://jenkins/jobs/myJob/1", "1", "null", "true")
                .get();
        Assert.assertFalse(buildInQueue.updateContentWith(refreshedBuildRunning));

//        Job refreshedBuildNotInQueueButDisabled = new JobBuilder().job("myJob", "grey", "http://jenkins/jobs/myJob", "false").get();
//        Assert.assertTrue(buildInQueue.updateContentWith(refreshedBuildNotInQueueButDisabled));
    }

    @Before
    public void setUp() throws Exception {

    }
}
