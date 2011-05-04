package org.codinjustu.tools.jenkins.view.annotation;

import org.codinjustu.tools.jenkins.exception.ConfigurationException;
import org.codinjustu.tools.jenkins.view.validator.ValidatorTypeEnum;

import javax.swing.*;
import java.lang.reflect.Field;

public class ValidatorAnnotationsUtils {

    private ValidatorAnnotationsUtils() {
    }


    public static void validate(Object formToValidate) throws ConfigurationException {
        for (Field field : formToValidate.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(GuiField.class)) {
                GuiField annotation = field.getAnnotation(GuiField.class);
                ValidatorTypeEnum[] validatorTypes = annotation.validators();
                for (ValidatorTypeEnum validatorType : validatorTypes) {
                    validatorType.getValidator().validate(getFieldObject(formToValidate, field));
                }
            }
        }
    }


    private static JComponent getFieldObject(Object formToValidate, Field field) {
        try {
            field.setAccessible(true);
            Object obj = field.get(formToValidate);
            field.setAccessible(false);
            if (obj instanceof JComponent) {
                return (JComponent) obj;
            }
            throw new RuntimeException("Field to be validated should be extends JComponent");
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
