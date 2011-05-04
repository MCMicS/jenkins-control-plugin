package org.codinjustu.tools.jenkins.model;

/**
 *
 */
public enum BuildStatusEnum {

    SUCCESS("SUCCESS"),
    FAILURE("FAILURE"),
    ABORTED("ABORTED"),
    NULL("NULL"),
    STABLE("STABLE"),
    UNSTABLE("UNSTABLE");
    private final String status;


    BuildStatusEnum(String status) {
        this.status = status;
    }


    public String getStatus() {
        return status;
    }
}
