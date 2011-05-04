package org.codinjustu.tools.jenkins.view;

import org.codinjustu.tools.jenkins.util.GuiUtil;

import javax.swing.*;

public enum JobStateEnum {
    BLUE("blue", "blue.png"),
    RED("red", "red.png"),
    GREY("grey", "grey.png"),
    DISABLED("disabled", "grey.png"),
    UNSTABLE("yellow", "yellow.png"),
    ABORTED("aborted", "grey.png");

    private String name;

    private Icon icon;


    JobStateEnum(String name, String iconFileName) {
        this.name = name;
        this.icon = GuiUtil.loadIcon(iconFileName);
    }


    public String getName() {
        return name;
    }


    public Icon getIcon() {
        return icon;
    }
}
