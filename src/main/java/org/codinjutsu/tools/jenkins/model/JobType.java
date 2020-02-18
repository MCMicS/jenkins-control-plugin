package org.codinjutsu.tools.jenkins.model;

import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import java.util.EnumMap;

public enum JobType {
    JOB,
    FOLDER,
    MULTI_BRANCH;

    private static final EnumMap<JobType, Icon> ICON_BY_TYPE = new EnumMap<>(JobType.class);

    static {
        ICON_BY_TYPE.put(FOLDER, GuiUtil.loadIcon("folder.png"));
        ICON_BY_TYPE.put(MULTI_BRANCH, GuiUtil.loadIcon("pipelinemultibranchproject.png"));
    }
}
