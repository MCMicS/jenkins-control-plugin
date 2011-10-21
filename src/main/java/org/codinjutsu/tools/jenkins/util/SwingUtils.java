/*
 * Copyright (c) 2011 David Boissier
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

package org.codinjutsu.tools.jenkins.util;

import org.codinjutsu.tools.jenkins.action.ThreadFunctor;

import javax.swing.*;


public class SwingUtils {
    public static void runInSwingThread(final ThreadFunctor threadFunctor) {
        if (SwingUtilities.isEventDispatchThread()) {
            threadFunctor.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        threadFunctor.run();
                    }
                });
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
