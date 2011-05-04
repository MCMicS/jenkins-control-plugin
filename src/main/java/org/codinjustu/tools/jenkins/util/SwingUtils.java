package org.codinjustu.tools.jenkins.util;

import org.codinjustu.tools.jenkins.action.ThreadFunctor;

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
