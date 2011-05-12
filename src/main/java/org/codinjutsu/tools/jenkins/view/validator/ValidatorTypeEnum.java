package org.codinjutsu.tools.jenkins.view.validator;

public enum ValidatorTypeEnum {

    STRICT_POSITIVE_INTEGER(new StrictPositiveIntegerValidator()),
    POSITIVE_INTEGER(new PositiveIntegerValidator()),
    URL(new UrlValidator()),
    NOTNULL(new NotNullValidator());

    private final UIValidator validator;


    ValidatorTypeEnum(UIValidator validator) {
        this.validator = validator;
    }


    public UIValidator getValidator() {
        return validator;
    }
}
