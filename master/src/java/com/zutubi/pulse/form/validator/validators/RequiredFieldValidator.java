package com.zutubi.pulse.form.validator.validators;

import com.zutubi.pulse.form.validator.ValidationException;

/**
 * <class-comment/>
 */
public class RequiredFieldValidator extends FieldValidatorSupport
{
    protected static final String REQUIRED = ".required";

    public void validate(Object obj) throws ValidationException
    {
        Object value = getFieldValue(getFieldName(), obj);
        if (value == null)
        {
            addFieldError(getFieldName(), getFieldName() + REQUIRED);
        }
    }
}
