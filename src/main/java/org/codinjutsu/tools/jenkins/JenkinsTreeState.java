package org.codinjutsu.tools.jenkins;

import com.intellij.util.xmlb.annotations.Tag;
import org.jdom.Element;

public class JenkinsTreeState {
    @Tag("treeState")
    public Element treeState;
}
