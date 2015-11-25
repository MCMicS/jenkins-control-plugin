package org.codinjutsu.tools.jenkins.view.util;

import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.border.Border;
import java.lang.reflect.Field;

/**
 * Created by Cezary Butler on 2015-11-13.
 */
public class WidgetBorderUtil {
    private static final Logger logger = Logger.getLogger(WidgetBorderUtil.class);

    private static Border INSTANCE;

    public static Border getBorderInstance(){
        if (INSTANCE == null) {
            INSTANCE = locateBorderInstance();
        }
        return INSTANCE;
    }

    public static Border locateBorderInstance() {
        try {
            return StatusBarWidget.WidgetBorder.INSTANCE;
        }catch(NoSuchFieldError nsf){
            final Border borderInstance = getUsingReflection();
            if (borderInstance != null) return borderInstance;
        }
        return fallbackMethod();
    }

    @NotNull
    private static JBEmptyBorder fallbackMethod() {
        return JBUI.Borders.empty(0, 2);
    }

    @Nullable
    private static Border getUsingReflection() {
        logger.info("Instance field not found, trying to locate one using reflection");
        try {
            final Field borderInstance = StatusBarWidget.WidgetBorder.class.getField("INSTANCE");
            return (Border) borderInstance.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e ) {
            logger.log(Level.WARN, "Exception occurred while trying to fetch border", e);
        }
        return null;
    }
}
