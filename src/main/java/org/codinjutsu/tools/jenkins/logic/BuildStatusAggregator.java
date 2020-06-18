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

package org.codinjutsu.tools.jenkins.logic;

import java.util.concurrent.atomic.AtomicInteger;

public class BuildStatusAggregator implements BuildStatusVisitor {

    private final AtomicInteger brokenBuilds = new AtomicInteger();

    private final AtomicInteger succeededBuilds = new AtomicInteger();

    private final AtomicInteger unstableBuilds = new AtomicInteger();

    private final AtomicInteger abortedBuilds = new AtomicInteger();

    private final AtomicInteger unknownBuilds = new AtomicInteger();

    private final AtomicInteger runningBuilds = new AtomicInteger();

    public static final BuildStatusAggregator EMPTY = new BuildStatusAggregator();

    @Override
    public void visitFailed() {
        brokenBuilds.getAndIncrement();
    }

    @Override
    public void visitSuccess() {
        succeededBuilds.getAndIncrement();
    }

    @Override
    public void visitUnstable() {
        unstableBuilds.getAndIncrement();
    }

    @Override
    public void visitUnknown() {
        unknownBuilds.getAndIncrement();
    }

    @Override
    public void visitAborted() {
        abortedBuilds.getAndIncrement();
    }

    @Override
    public void visitBuilding() {
        runningBuilds.getAndIncrement();
    }

    public int getBrokenBuilds() {
        return brokenBuilds.get();
    }

    public int getUnstableBuilds() {
        return unstableBuilds.get();
    }

    public int getSucceededBuilds() {
        return succeededBuilds.get();
    }

    public int getRunningBuilds() {
        return runningBuilds.get();
    }

    public boolean hasNoResults() {
        return sumAll() == 0;
    }

    public int sumAll() {
        return succeededBuilds.get() + unstableBuilds.get() + brokenBuilds.get() + abortedBuilds.get() + unknownBuilds.get() + runningBuilds.get();
    }
}
