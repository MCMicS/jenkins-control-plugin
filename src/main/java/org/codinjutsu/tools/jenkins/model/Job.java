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

import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.EmptyIcon;
import icons.JenkinsControlIcons;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.Value;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * TODO mcmics: use {@link Value}
 */
@Builder(toBuilder = true)
//@Value
@Data
public class Job {

    private static final Map<String, Icon> ICON_BY_JOB_HEALTH_MAP = new HashMap<>();
    public static final String WORKFLOW_JOB = "WorkflowJob";

    static {
        ICON_BY_JOB_HEALTH_MAP.put("health-00to19", JenkinsControlIcons.Health.HEALTH_00_TO_19);
        ICON_BY_JOB_HEALTH_MAP.put("health-20to39", JenkinsControlIcons.Health.HEALTH_20_TO_39);
        ICON_BY_JOB_HEALTH_MAP.put("health-40to59", JenkinsControlIcons.Health.HEALTH_40_TO_59);
        ICON_BY_JOB_HEALTH_MAP.put("health-60to79", JenkinsControlIcons.Health.HEALTH_60_TO_79);
        ICON_BY_JOB_HEALTH_MAP.put("health-80plus", JenkinsControlIcons.Health.HEALTH_60_PLUS);
        ICON_BY_JOB_HEALTH_MAP.put("null", EmptyIcon.ICON_16);
    }

    @NotNull
    private final String name;
    @Builder.Default
    @NotNull
    private final JobType jobType = JobType.JOB;
    private final boolean buildable;
    @Builder.Default
    @Nullable
    private final String displayName = null;
    @Builder.Default
    @Nullable
    private final String fullDisplayName = null;
    @NotNull
    private final String fullName;
    @NotNull
    private final String url;
    @Singular
    @NotNull
    private final List<JobParameter> parameters;
    private boolean inQueue;
    @Builder.Default
    @Nullable
    private String color = null;
    @Builder.Default
    @Nullable
    private Health health = null;
    @Builder.Default
    @Nullable
    private Build lastBuild = null;
    @Builder.Default
    @NotNull
    private List<Build> lastBuilds = new LinkedList<>();
    @Builder.Default
    @NotNull
    private List<Job> nestedJobs = new LinkedList<>();
    @Builder.Default
    @NotNull
    private EnumSet<BuildType> availableBuildTypes = EnumSet.noneOf(BuildType.class);

    @NotNull
    public Icon getHealthIcon() {
        if (health == null) {
            return ICON_BY_JOB_HEALTH_MAP.get("null");
        }
        return ICON_BY_JOB_HEALTH_MAP.getOrDefault(health.getLevel(), ICON_BY_JOB_HEALTH_MAP.get("null"));
    }

    @NotNull
    public String getHealthDescription() {
        if (health == null) {
            return "";
        }
        return ObjectUtils.notNull(health.getDescription(), "");
    }

    public void updateContentWith(Job updatedJob) {
        this.color = updatedJob.getColor();
        this.health = updatedJob.getHealth();
        this.inQueue = updatedJob.isInQueue();
        this.lastBuild = updatedJob.getLastBuild();
        this.lastBuilds = updatedJob.getLastBuilds();
    }

    @NotNull
    public String getNameToRenderSingleJob() {
        return Optional.ofNullable(getFullDisplayName()).filter(Predicate.not(StringUtils::isEmpty))
                .orElseGet(this::preferDisplayName);
    }

    @NotNull
    public String preferDisplayName() {
        return Optional.ofNullable(getDisplayName()).filter(Predicate.not(StringUtils::isEmpty))
                .orElseGet(this::getName);
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public boolean hasParameter(String name) {
        if (hasParameters()) {
            for (JobParameter parameter : parameters) {
                if (parameter.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Value
    public static class Health {

        @NotNull
        private final String level;
        @Nullable
        private final String description;
    }
}
