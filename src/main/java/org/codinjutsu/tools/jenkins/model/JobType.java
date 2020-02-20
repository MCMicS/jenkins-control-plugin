package org.codinjutsu.tools.jenkins.model;

import com.intellij.icons.AllIcons;
import icons.JenkinsControlIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.EnumMap;

public enum JobType {
    JOB,
    FOLDER,
    MULTI_BRANCH;

    private static final EnumMap<JobType, Icon> ICON_BY_TYPE = new EnumMap<>(JobType.class);

    static {
        ICON_BY_TYPE.put(JOB, JenkinsControlIcons.Job.GREY);
        ICON_BY_TYPE.put(FOLDER, AllIcons.Nodes.Folder);
        ICON_BY_TYPE.put(MULTI_BRANCH, AllIcons.Vcs.Branch);
    }

    @NotNull
    public Icon getIcon() {
        return ICON_BY_TYPE.get(this);
    }
}
