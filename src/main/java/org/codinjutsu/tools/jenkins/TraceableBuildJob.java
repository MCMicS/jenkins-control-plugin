package org.codinjutsu.tools.jenkins;

import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.model.Job;

import com.google.common.base.Objects;

public class TraceableBuildJob {
    public final Job job;
    private final Runnable runBuild;
    private Integer numBuildTriesLeft;
    private Boolean passedAnyBuild = false;

    TraceableBuildJob(Job job, Runnable runBuildCommand, Integer numTries) {
        this.job = job;
        this.numBuildTriesLeft = numTries - 1;//view execute jobs itself after registering
        this.runBuild = runBuildCommand;
    }

    public void someBuildFinished(Build build) {
        if (buildBelongsToThisJob(build)) {
            updatePassedAnyBuildStatus(build);
            if (shouldStillTryBuilding()) {
                runBuild.run();
                numBuildTriesLeft--;
            }
        }
    }

    private void updatePassedAnyBuildStatus(Build build) {
        passedAnyBuild = passedAnyBuild || build.getStatus().equals(BuildStatusEnum.SUCCESS);
    }

    private boolean shouldStillTryBuilding() {
        return numBuildTriesLeft > 0 && !passedAnyBuild;
    }

    public boolean isDone(){
        return !shouldStillTryBuilding();
    }

    private boolean buildBelongsToThisJob(Build build) {
        return build.getUrl()
                .contains(job.getUrl());
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass() == o.getClass() && ((TraceableBuildJob) o).job.getUrl().equals(job.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(job.getName(), job.getUrl());
    }
}

