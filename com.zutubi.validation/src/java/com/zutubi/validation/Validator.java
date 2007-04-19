package com.zutubi.validation;

/**
 * <class-comment/>
 */
public interface Validator
{
    ValidationContext getValidationContext();

    void setValidationContext(ValidationContext ctx);

    void validate(Object obj) throws ValidationException;

}
