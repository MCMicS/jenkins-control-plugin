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

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.util.RssUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.input.sax.XMLReaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RssParser {

    private static final Logger LOG = Logger.getInstance(RssParser.class);

    public final SimpleDateFormat rssDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final String RSS_ENTRY = "entry";
    private static final String RSS_TITLE = "title";
    private static final String RSS_LINK = "link";
    private static final String RSS_LINK_HREF = "href";
    private static final String RSS_PUBLISHED = "published";

    @NotNull
    public Map<String, Build> loadJenkinsRssLatestBuilds(String rssData) {
        return createLatestBuildList(buildDocument(rssData));
    }

    @NotNull
    private Document buildDocument(@Nullable String xmlData) {
        if (StringUtils.isEmpty(xmlData)) {
            LOG.error("Empty XML data");
            throw new IllegalStateException("Empty XML data");
        }

        try(Reader jenkinsDataReader = new StringReader(xmlData)) {
            return new SAXBuilder(XMLReaders.NONVALIDATING).build(jenkinsDataReader);
        } catch (JDOMException e) {
            LOG.error("Invalid data received from the Jenkins Server. Actual :\n" + xmlData, e);
            throw new RuntimeException("Invalid data received from the Jenkins Server. Please retry");
        } catch (IOException e) {
            LOG.error("Error during analyzing the Jenkins data.", e);
            throw new RuntimeException("Error during analyzing the Jenkins data.");
        }
    }

    @NotNull
    private Map<String, Build> createLatestBuildList(@NotNull Document doc) {
        final Map<String, Build> buildMap = new LinkedHashMap<>();
        final Element rootElement = doc.getRootElement();

        final List<Element> elements = rootElement.getChildren(RSS_ENTRY, rootElement.getNamespace());
        for (Element element : elements) {
            final String title = Optional.ofNullable(element.getChildText(RSS_TITLE, rootElement.getNamespace()))
                    .orElse(StringUtils.EMPTY);
            final String publishedBuild = element.getChildText(RSS_PUBLISHED, rootElement.getNamespace());
            final Element buildUrlElement = element.getChild(RSS_LINK, rootElement.getNamespace());
            final Optional<String> buildUrl = Optional.ofNullable(buildUrlElement.getAttributeValue(RSS_LINK_HREF));
            // maybe load build from jenkins with needed info
            final String jobName = RssUtil.extractBuildJob(title);
            final int number = buildUrl.map(RssUtil::extractBuildNumber).orElse(-1);
            final BuildStatusEnum status = RssUtil.extractStatus(title);

            if (!BuildStatusEnum.NULL.equals(status)) {
                buildMap.put(jobName, Build.createBuildFromRss(buildUrl.orElse(StringUtils.EMPTY), number,
                        status.getStatus(), false, publishedBuild, title, rssDateFormat));
            }
        }

        return buildMap;
    }

    @NotNull
    SimpleDateFormat getDateFormat() {
        return rssDateFormat;
    }
}
