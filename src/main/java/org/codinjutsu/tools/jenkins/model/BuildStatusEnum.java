/*
 * Copyright (c) 2011 David Boissier
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
