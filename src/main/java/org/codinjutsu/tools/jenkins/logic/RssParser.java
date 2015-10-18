/*
 * Copyright (c) 2013 David Boissier
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

//import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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

    private Document buildDocument(String xmlData) {
        if (StringUtils.isEmpty(xmlData)) {
            LOG.error("Empty XML data");
            throw new IllegalStateException("Empty XML data");
        }

        try(Reader jenkinsDataReader = new StringReader(xmlData)) {
            return new SAXBuilder(false).build(jenkinsDataReader);
        } catch (JDOMException e) {
            LOG.error("Invalid data received from the Jenkins Server. Actual :\n" + xmlData, e);
            throw new RuntimeException("Invalid data received from the Jenkins Server. Please retry");
        } catch (IOException e) {
            LOG.error("Error during analyzing the Jenkins data.", e);
            throw new RuntimeException("Error during analyzing the Jenkins data.");
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
