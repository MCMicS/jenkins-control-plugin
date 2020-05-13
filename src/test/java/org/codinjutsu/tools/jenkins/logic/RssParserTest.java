package org.codinjutsu.tools.jenkins.logic;

import com.intellij.util.ResourceUtil;
import org.assertj.core.api.Assertions;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.util.DateUtil;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class RssParserTest {

    private final RssParser rssParser = new RssParser();

    @Test
    public void loadJenkinsRssLatestBuildsSingleEntry() throws ParseException {
        final String rssEntry = //
                "<feed xmlns=\"http://www.w3.org/2005/Atom\">" +
                        "    <entry>\n" +
                        "        <title>Single Build #42 (stable)</title>\n" +
                        "        <link type=\"text/html\" href=\"http://jenkins.example.org/job/single_build/42/\" rel=\"alternate\"/>\n" +
                        "        <id>tag:hudson.dev.java.net,2008:http://jenkins.example.org/job/single_build/</id>\n" +
                        "        <published>2020-05-12T20:07:51Z</published>\n" +
                        "        <updated>2020-05-12T21:07:51Z</updated>\n" +
                        "    </entry>" +
                        "</feed>";
        final Map<String, Build> singleEntry = rssParser.loadJenkinsRssLatestBuilds(rssEntry);
        assertThat(singleEntry).hasSize(1);
        final Date buildDate = DateUtil.RSS_DATE_FORMAT.parse("2020-05-12T20:07:51Z");
        final Build build = Build.builder()
                .url("http://jenkins.example.org/job/single_build/42/")
                .number(42)
                .status(BuildStatusEnum.SUCCESS)
                .buildDate(buildDate)
                .building(false)
                .timestamp(new Date(0L))
                .duration(0L)
                .building(false)
                .message("Single Build #42 (stable)")
                .build();
        assertThat(singleEntry.keySet()).containsOnly("Single Build");
        assertThat(singleEntry.values()).containsOnly(build);
    }

    @Test
    public void loadJenkinsRssLatestBuilds() throws IOException, ParseException {
        final String rss = ResourceUtil.loadText(ResourceUtil.getResourceAsStream(this.getClass(),
                "org\\codinjutsu\\tools\\jenkins\\logic", "JenkinsRss.xml"));
        final Map<String, Build> entries = rssParser.loadJenkinsRssLatestBuilds(rss);
        final Build longBrokenBuild = Build.builder()
                .url("http://ci.jenkins-ci.org/job/TESTING-HUDSON-7434/2/")
                .number(2)
                .status(BuildStatusEnum.FAILURE)
                .buildDate(DateUtil.RSS_DATE_FORMAT.parse("2011-03-02T05:27:56Z"))
                .building(false)
                .timestamp(new Date(0L))
                .duration(0L)
                .building(false)
                .message("TESTING-HUDSON-7434 #2 (broken for a long time)")
                .build();
        final Build backToNormal = Build.builder()
                .url("http://ci.jenkins-ci.org/job/infra_jenkins-ci.org_webcontents/2/")
                .number(2)
                .status(BuildStatusEnum.SUCCESS)
                .buildDate(DateUtil.RSS_DATE_FORMAT.parse("2011-02-02T00:49:58Z"))
                .building(false)
                .timestamp(new Date(0L))
                .duration(0L)
                .building(false)
                .message("infra_jenkins-ci.org_webcontents #2 (back to normal)")
                .build();
        final Build unknownStatus = Build.builder()
                .url("http://ci.jenkins-ci.org/job/jenkins_main_trunk/600/")
                .number(600)
                .status(BuildStatusEnum.NULL)
                .buildDate(DateUtil.RSS_DATE_FORMAT.parse("2011-03-15T23:30:58Z"))
                .building(false)
                .timestamp(new Date(0L))
                .duration(0L)
                .building(false)
                .message("jenkins_main_trunk #600 (?)")
                .build();
        assertThat(entries).hasSize(7);
        assertThat(entries.keySet()).contains("infra_jenkins-ci.org_webcontents",
                "TESTING-HUDSON-7434");
        assertThat(entries.keySet()).doesNotContain("jenkins_main_trunk");
        assertThat(entries.values()).contains(backToNormal, longBrokenBuild);
        assertThat(entries.values()).doesNotContain(unknownStatus);
    }

    @Test
    public void loadJenkinsRssLatestBuildsDifferentVersionSchema() throws IOException, ParseException {
        final String rss = ResourceUtil.loadText(ResourceUtil.getResourceAsStream(this.getClass(),
                "org\\codinjutsu\\tools\\jenkins\\logic", "RssWithVersionNumbering.xml"));
        final Map<String, Build> entries = rssParser.loadJenkinsRssLatestBuilds(rss);
        final Build normalVersionNumber = Build.builder()
                .url("http://localhost:8080/job/Version%20Number/job/VersionNumber/3/")
                .number(3)
                .status(BuildStatusEnum.FAILURE)
                .buildDate(DateUtil.RSS_DATE_FORMAT.parse("2020-05-07T18:18:13Z"))
                .building(false)
                .timestamp(new Date(0L))
                .duration(0L)
                .building(false)
                .message("TESTING-HUDSON-7434 #2 (broken for a long time)")
                .build();
        final Build customVersionNumber = Build.builder()
                .url("http://localhost:8080/job/Version%20Number/job/With%20Build%20Display/5/")
                .number(5)
                .status(BuildStatusEnum.SUCCESS)
                .buildDate(DateUtil.RSS_DATE_FORMAT.parse("22020-05-07T18:18:38Z"))
                .building(false)
                .timestamp(new Date(0L))
                .duration(0L)
                .building(false)
                .message("infra_jenkins-ci.org_webcontents #2 (back to normal)")
                .build();
        assertThat(entries).hasSize(2);
        assertThat(entries.values()).contains(normalVersionNumber, customVersionNumber);
    }
}
