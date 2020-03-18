package org.codinjutsu.tools.jenkins.util;

import lombok.experimental.UtilityClass;
import org.codinjutsu.tools.jenkins.model.Job;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class CollectionUtil {

    @NotNull
    public List<Job> flattenedJobs(@NotNull List<Job> jobs) {
        return jobs.stream().flatMap(CollectionUtil::flattenedJobs).collect(Collectors.toList());
    }

    @NotNull
    public Stream<Job> flattenedJobs(Job job) {
        return Stream.concat(
                Stream.of(job),
                job.getNestedJobs().stream().flatMap(CollectionUtil::flattenedJobs));
    }
}
