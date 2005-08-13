package com.cinnamonbob.xwork.validator.validators;

import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;
import com.opensymphony.xwork.validator.ValidationException;

/**
 *
 *
 */
public class IntegralValidator extends FieldValidatorSupport
{
    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        try
        {
            Long.parseLong((String)obj);
        }
        catch (NumberFormatException e)
        {
            addFieldError(getFieldName(), e.getMessage());
        }
    }
}
