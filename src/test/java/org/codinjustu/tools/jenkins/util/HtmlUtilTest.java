package org.codinjustu.tools.jenkins.util;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class HtmlUtilTest {

    @Test
    public void test_createHtmlLinkMessage() throws Exception {
        assertThat(HtmlUtil.createHtmlLinkMessage("a link label", "url"),
                equalTo("<html><a href='url'>a link label</a></html>"));
    }
}
