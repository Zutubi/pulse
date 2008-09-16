package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * A trivial no operation validator that does nothing.
 */
public class NoopFieldValidator extends FieldValidatorSupport
{
    public void validateField(Object value) throws ValidationException
    {

    }
}