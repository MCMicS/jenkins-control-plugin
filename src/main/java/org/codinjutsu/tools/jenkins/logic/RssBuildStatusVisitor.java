package org.codinjutsu.tools.jenkins.logic;

import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;

public class RssBuildStatusVisitor implements BuildStatusVisitor {

    private BuildStatusEnum buildStatusEnum;


    public void visitFailed() {
        buildStatusEnum = BuildStatusEnum.FAILURE;
    }

    public void visitSuccess() {
        buildStatusEnum = BuildStatusEnum.SUCCESS;
    }

    public void visitUnkown() {
        buildStatusEnum = BuildStatusEnum.NULL;
    }

    public void visitAborted() {
        buildStatusEnum = BuildStatusEnum.ABORTED;
    }

    public BuildStatusEnum getStatus() {
        return buildStatusEnum;
    }
}
