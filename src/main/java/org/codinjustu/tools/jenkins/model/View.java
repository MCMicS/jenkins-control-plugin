package org.codinjustu.tools.jenkins.model;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class View {

    private final String name;

    private final String url;


    private View(String name, String url) {
        this.name = name;
        this.url = url;
    }


    public String getName() {
        return name;
    }


    public String getUrl() {
        return url;
    }


    @Override
    public boolean equals(Object obj) {
        return reflectionEquals(this, obj);
    }


    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }


    @Override
    public String toString() {
        return reflectionToString(this, SHORT_PREFIX_STYLE);
    }


    public static View createView(String viewName, String viewUrl) {
        return new View(viewName, viewUrl);
    }
}
