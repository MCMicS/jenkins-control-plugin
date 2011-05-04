package org.codinjustu.tools.jenkins.model;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.codinjustu.tools.jenkins.model.BuildStatusEnum.SUCCESS;

public class Build {

    private String buildUrl;

    private int number;

    private BuildStatusEnum status;

    private boolean building;


    private Build(String buildUrl, int number, BuildStatusEnum status, boolean isBuilding) {
        this.buildUrl = buildUrl;
        this.number = number;
        this.status = status;
        this.building = isBuilding;
    }


    public String getUrl() {
        return buildUrl;
    }


    public int getNumber() {
        return number;
    }


    public BuildStatusEnum getStatus() {
        return status;
    }


    public String getStatusValue() {
        return status.getStatus();
    }


    boolean isSuccess() {
        return SUCCESS.equals(status);
    }


    public boolean isBuilding() {
        return building;
    }


    public boolean isDisplayable(Build currentBuild) {
        return this.isAfter(currentBuild) &&
                (BuildStatusEnum.ABORTED.equals(this.getStatus()) || !this.hasSameSuccessThan(currentBuild));
    }


    public boolean isAfter(Build aBuild) {
        return this.getNumber() > aBuild.getNumber();
    }


    private boolean hasSameSuccessThan(Build aBuild) {
        return this.isSuccess() && aBuild.isSuccess();
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


    public static Build createBuild(String buildUrl, String number, String status, String isBuilding) {
        if (status == null || "null".equals(status)) {
            status = "NULL";
        }

        BuildStatusEnum buildStatusEnum;
        try {
            buildStatusEnum = BuildStatusEnum.valueOf(status.toUpperCase());

        } catch (IllegalArgumentException ex) {
            System.out.println("Unkown status : " + status);
            buildStatusEnum = BuildStatusEnum.NULL;
        }
        return new Build(buildUrl, Integer.valueOf(number), buildStatusEnum, Boolean.valueOf(isBuilding));
    }
}
