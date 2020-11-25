package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class JenkinsControlIcons {

    private static final String ICON_FOLDER = "/images/";

    public static final Icon JENKINS_TOOLWINDOW_ICON = getIcon("toolWindow_jenkins_icon.svg");
    public static final Icon TOOLWINDOW_TEST_RESULTS = getIcon("toolWindowGroupByTestProduction.svg");

    public static final Icon RSS = getIcon("rss.svg");

    public static final Icon LOAD_BUILDS = getIcon("builds.svg");

    public static Icon getIcon(String iconFilename) {
        return IconLoader.getIcon(ICON_FOLDER + iconFilename, JenkinsControlIcons.class);
    }

    public static final class Health {
        public static final Icon HEALTH_00_TO_19 = getIcon("health-00to19.svg");
        public static final Icon HEALTH_20_TO_39 = getIcon("health-20to39.svg");
        public static final Icon HEALTH_40_TO_59 = getIcon("health-40to59.svg");
        public static final Icon HEALTH_60_TO_79 = getIcon("health-60to79.svg");
        public static final Icon HEALTH_60_PLUS = getIcon("health-80plus.svg");
    }

    public static final class Job {
        public static final Icon BLUE = getIcon("blue.svg");
        public static final Icon YELLOW = getIcon("yellow.svg");
        public static final Icon RED = getIcon("red.svg");
        public static final Icon GREY = getIcon("grey.svg");
        public static final Icon GREEN = getIcon("green.svg");
    }
}
