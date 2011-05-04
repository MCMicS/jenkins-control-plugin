package org.codinjustu.tools.jenkins.util;

public class HtmlUtil {

    private HtmlUtil() {
    }


    public static String createHtmlLinkMessage(String linkLabel, String linkUrl) {
        return "<html><a href='" + linkUrl + "'>" + linkLabel + "</a></html>";
    }
}
