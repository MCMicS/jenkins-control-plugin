package org.codinjustu.tools.jenkins.view.annotation;

import org.codinjustu.tools.jenkins.view.validator.ValidatorTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GuiField {

    ValidatorTypeEnum[] validators();
}
