/*
 * Copyright (c) 2013 David Boissier
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

import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Currently missing color: nobuilt
 *
 * @see <a href="https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/hudson/model/BallColor.java">Jenkins Color</a>
 * @see <a href="https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/hudson/model/StatusIcon.java>Jenkins Status</a>
 */
@RequiredArgsConstructor
public enum BuildStatusEnum {

    FAILURE("Failure", Color.RED),
    UNSTABLE("Unstable", Color.YELLOW),
    ABORTED("Aborted", Color.ABORTED),
    SUCCESS("Success", Color.BLUE),
    STABLE("Stable", Color.BLUE),
    NULL("Null"),
    // TODO: handle the folder-case explicitly. @mcmics: use better Folder Detection
    // instead of simply making it a BuildStatusEnum so that the icon renders
    FOLDER("Folder");


    private static final Logger log = Logger.getLogger(BuildStatusEnum.class);

    private final String status;
    private final Color color;


    BuildStatusEnum(String status) {
        this(status, Color.DISABLED);
    }

    public static BuildStatusEnum parseStatus(String status) {
        BuildStatusEnum buildStatusEnum;
        try {
            if (status == null || "null".equals(status)) {
                status = "NULL";
            }
            buildStatusEnum = valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            log.info("Unsupported status : " + status, ex);
            buildStatusEnum = NULL;
        }
        return buildStatusEnum;
    }

    /**
     * Parse status from color
     */
    public static BuildStatusEnum getStatus(String jobColor) {
        if (null == jobColor) {
            return NULL;
        }
        BuildStatusEnum[] jobStates = values();
        for (BuildStatusEnum jobStatus : jobStates) {
            if (jobStatus.getColor().isForJobColor(jobColor)) {
                return jobStatus;
            }
        }

        return NULL;
    }


    public String getStatus() {
        return status;
    }

    @NotNull
    public Color getColor() {
        return color;
    }
}
