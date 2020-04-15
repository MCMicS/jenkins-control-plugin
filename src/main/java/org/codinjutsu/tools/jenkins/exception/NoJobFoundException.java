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

package org.codinjutsu.tools.jenkins.exception;

import org.codinjutsu.tools.jenkins.model.Job;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

public class NoJobFoundException extends RuntimeException {

    public NoJobFoundException(@NotNull Job job) {
        super(createMessage(job));
    }

    public NoJobFoundException(@NotNull Job job, Throwable throwable) {
        super(createMessage(job), throwable);
    }

    @NotNull
    @Override
    public String getMessage() {
        final String message = super.getMessage();
        return message == null ? "Unknown Error" : message;
    }

    @NotNull
    private static String createMessage(@NotNull Job job) {
        return MessageFormat.format("Could not find Job data for: {0} [{1}]", job.getName(), job.getUrl());
    }
}
