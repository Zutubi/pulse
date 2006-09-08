package com.zutubi.pulse.form.validator;

/**
 * <class-comment/>
 */
public interface Validator
{
    void setValidatorContext(ValidatorContext context);

    void validate(Object obj) throws ValidationException;
}
