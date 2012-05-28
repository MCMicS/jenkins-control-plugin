/*
 * Copyright (c) 2012 David Boissier
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

/**
 *
 */
public enum BuildStatusEnum {

    SUCCESS("Success", "blue"),
    FAILURE("Failure", "red"),
    NULL("Null", "disabled"),
    UNSTABLE("Unstable", "yellow"),
    STABLE("Stable", "blue"),
    ABORTED("Aborted", "aborted");
    private final String status;
    private final String color;


    BuildStatusEnum(String status, String color) {
        this.status = status;
        this.color = color;
    }

    public static BuildStatusEnum parseStatus(String status) {
        BuildStatusEnum buildStatusEnum;
        try {
            if (status == null || "null".equals(status)) {
                status = "NULL";
            }
            buildStatusEnum = valueOf(status.toUpperCase());

        } catch (IllegalArgumentException ex) {
            System.out.println("Unknown status : " + status);
            buildStatusEnum = NULL;
        }
        return buildStatusEnum;
    }


    public String getStatus() {
        return status;
    }


    public String getColor() {
        return color;
    }


}
