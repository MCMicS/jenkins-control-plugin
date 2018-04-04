package org.codinjutsu.tools.jenkins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.codinjutsu.tools.jenkins.model.Build;

public class JobTracker {
    private static JobTracker ourInstance = new JobTracker();

    public static JobTracker getInstance() {
        return ourInstance;
    }

    private ArrayList<TraceableBuildJob> buildJobs = new ArrayList<>();

    private JobTracker() {
    }

    public void registerJob(TraceableBuildJob buildJob) {
        if (!buildJobs.contains(buildJob)) {
            buildJobs.add(buildJob);
        } else { //remove existing job if a new one was submitted from GUI
            buildJobs.removeIf(buildJob::equals);
            buildJobs.add(buildJob);
        }
    }

    public void onNewFinishedBuilds(Map<String, Build> finishedBuilds) {
        notifyJobsAboutNewFinishedBuilds(finishedBuilds.values());
        removeDoneJobs();
    }

    private void removeDoneJobs() {
        buildJobs.stream()
                .filter(TraceableBuildJob::isDone)
                .forEach(buildJobs::remove);
    }

    private void notifyJobsAboutNewFinishedBuilds(Collection<Build> finishedBuilds) {
        buildJobs.forEach(traceableBuildJob -> finishedBuilds.forEach(traceableBuildJob::someBuildFinished));
    }
}


