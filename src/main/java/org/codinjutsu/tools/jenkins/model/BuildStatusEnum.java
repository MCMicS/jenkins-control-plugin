package org.codinjutsu.tools.jenkins.model;

import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;

/**
 *
 */
public enum BuildStatusEnum {

    SUCCESS("Success", "blue", "blue.png"),
    FAILURE("Failure", "red", "red.png"),
    NULL("Null", "disabled", "grey.png"),
    UNSTABLE("Unstable", "yellow", "yellow.png"),
    STABLE("Stable", "blue", "blue.png"),
    ABORTED("Aborted","aborted", "grey.png")
    ;
    private final String status;
    private final String color;
    private final Icon icon;


    BuildStatusEnum(String status, String color, String iconName) {
        this.status = status;
        this.color = color;
        this.icon = GuiUtil.loadIcon(iconName);
    }


    public String getStatus() {
        return status;
    }


    public String getColor() {
        return color;
    }


    public Icon getIcon() {
        return icon;
    }
}
