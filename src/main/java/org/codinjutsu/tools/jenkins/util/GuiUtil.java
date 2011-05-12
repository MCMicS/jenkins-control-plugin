package org.codinjutsu.tools.jenkins.util;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class GuiUtil {

    private static final String ICON_FOLDER = "/images/";


    private GuiUtil() {
    }


    public static Icon loadIcon(String iconFilename) {
        return IconLoader.findIcon(ICON_FOLDER + iconFilename);
    }


    public static void showErrorDialog(String errorMessage, String title) {
        Messages.showErrorDialog(errorMessage, title);
    }
}
