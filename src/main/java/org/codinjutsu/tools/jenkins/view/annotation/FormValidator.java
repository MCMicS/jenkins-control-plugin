package org.codinjutsu.tools.jenkins.view.annotation;

import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.view.JenkinsConfigurationPanel;
import org.codinjutsu.tools.jenkins.view.validator.UIValidator;
import org.codinjutsu.tools.jenkins.view.validator.ValidatorTypeEnum;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FormValidator<T extends JComponent> {

    private JenkinsConfigurationPanel formToValidate;
    private HashMap<T, UIValidator<T>> uiValidatorByUiComponent = new HashMap<T, UIValidator<T>>();


    private FormValidator(JenkinsConfigurationPanel formToValidate) {
        this.formToValidate = formToValidate;
    }

    public static FormValidator init(JenkinsConfigurationPanel formToValidate) {
        return new FormValidator(formToValidate);
    }

    public FormValidator  addValidator(T componentToValidate, UIValidator<T> validator) {
        uiValidatorByUiComponent.put(componentToValidate, validator);
        return this;
    }


    public void validate() throws ConfigurationException {
        for (Field field : formToValidate.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(GuiField.class)) {
                GuiField annotation = field.getAnnotation(GuiField.class);
                ValidatorTypeEnum[] validatorTypes = annotation.validators();
                for (ValidatorTypeEnum validatorType : validatorTypes) {
                    validatorType.getValidator().validate(getFieldObject(formToValidate, field));
                }
            }
        }

        for (Map.Entry<T, UIValidator<T>> entry : uiValidatorByUiComponent.entrySet()) {
            entry.getValue().validate(entry.getKey());
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
