package org.codinjutsu.tools.jenkins.logic;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.util.RssUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RssParser {

    private static final Logger LOG = Logger.getLogger(RssParser.class);

    private static final String RSS_ENTRY = "entry";
    private static final String RSS_TITLE = "title";
    private static final String RSS_LINK = "link";
    private static final String RSS_LINK_HREF = "href";
    private static final String RSS_PUBLISHED = "published";

    public RssParser() {
    }

    public Map<String, Build> loadJenkinsRssLatestBuilds(String rssData) {
        Document doc = buildDocument(rssData);
        return createLatestBuildList(doc);
    }

    private Document buildDocument(String jenkinsXmlData) {
        Reader jenkinsDataReader = new StringReader(jenkinsXmlData);
        try {
            return new SAXBuilder(false).build(jenkinsDataReader);
        } catch (JDOMException e) {
            LOG.error("Invalid data received from the Jenkins Server. Actual :\n" + jenkinsXmlData, e);
            throw new RuntimeException("Invalid data received from the Jenkins Server. Please retry");
        } catch (IOException e) {
            LOG.error("Error during analyzing the Jenkins data.", e);
            throw new RuntimeException("Error during analyzing the Jenkins data.");
        } finally {
            IOUtils.closeQuietly(jenkinsDataReader);
        }
    }

    private Map<String, Build> createLatestBuildList(Document doc) {

        Map<String, Build> buildMap = new LinkedHashMap<String, Build>();
        Element rootElement = doc.getRootElement();

        List<Element> elements = rootElement.getChildren(RSS_ENTRY, rootElement.getNamespace());
        for (Element element : elements) {
            String title = element.getChildText(RSS_TITLE, rootElement.getNamespace());
            String publishedBuild = element.getChildText(RSS_PUBLISHED, rootElement.getNamespace());
            String jobName = RssUtil.extractBuildJob(title);
            String number = RssUtil.extractBuildNumber(title);
            BuildStatusEnum status = RssUtil.extractStatus(title);
            Element linkElement = element.getChild(RSS_LINK, rootElement.getNamespace());
            String link = linkElement.getAttributeValue(RSS_LINK_HREF);

            if (!BuildStatusEnum.NULL.equals(status)) {
                buildMap.put(jobName, Build.createBuildFromRss(link, number, status.getStatus(), Boolean.FALSE.toString(), publishedBuild, title));

            }

        }

        return buildMap;
    }
}
