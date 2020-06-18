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

import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;

public class RssBuildStatusVisitor implements BuildStatusVisitor {

    private BuildStatusEnum buildStatusEnum;


    @Override
    public void visitFailed() {
        buildStatusEnum = BuildStatusEnum.FAILURE;
    }

    @Override
    public void visitSuccess() {
        buildStatusEnum = BuildStatusEnum.SUCCESS;
    }

    @Override
    public void visitUnstable() {
        buildStatusEnum = BuildStatusEnum.UNSTABLE;
    }

    @Override
    public void visitUnknown() {
        buildStatusEnum = BuildStatusEnum.NULL;
    }

    @Override
    public void visitAborted() {
        buildStatusEnum = BuildStatusEnum.ABORTED;
    }

    @Override
    public void visitBuilding() {
        // Not used
    }

    public BuildStatusEnum getStatus() {
        return buildStatusEnum;
    }
}
