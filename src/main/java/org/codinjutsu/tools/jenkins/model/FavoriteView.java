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


import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FavoriteView extends View {

    public static String FAVORITE = "FAVORITE";


    private final List<Job> favoriteJobs = new LinkedList<Job>();

    public FavoriteView() {
        super(FAVORITE, null, false);
    }

    public void add(Job job) {
        favoriteJobs.add(job);
    }

    public List<Job> getJobs() {
        return favoriteJobs;
    }

    public void remove(Job selectedJob) {
        for (Iterator<Job> iterator = favoriteJobs.iterator(); iterator.hasNext(); ) {
            Job job = iterator.next();
            if (StringUtils.equals(selectedJob.getName(), job.getName())) {
                iterator.remove();
            }
        }
    }

    public boolean isEmpty() {
        return favoriteJobs.isEmpty();
    }
}
