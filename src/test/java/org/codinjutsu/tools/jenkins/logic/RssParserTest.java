package org.codinjutsu.tools.jenkins.logic;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.util.DateUtil;
import org.junit.Test;

import java.util.Map;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class RssParserTest {
    @Test
    public void testLoadJenkinsRssLatestBuilds() throws Exception {

        String rssLatest = IOUtils.toString(getClass().getResourceAsStream("RssParserTest_loadData.xml"));
        Map<String, Build> parsedRss = new RssParser().loadJenkinsRssLatestBuilds(rssLatest);

        Assert.assertEquals(7, parsedRss.size());

        Build build = new Build();
        build.setUrl("http://ci.jenkins-ci.org/job/gerrit_master/170/");
        build.setNumber(170);
        build.setBuilding(false);
        build.setBuildDate("2011-03-16T14:28:59Z", DateUtil.RSS_DATE_FORMAT);
        build.setStatus("Failure");
        build.setMessage("gerrit_master #170 (broken since build #165)");
        assertReflectionEquals(build, parsedRss.get("gerrit_master"));
    }
}
