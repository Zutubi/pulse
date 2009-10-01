package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

public class URLValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new URLValidator();
    }

    public void testHttpsURL() throws ValidationException
    {
        validator.validate(new FieldProvider("https://www.zutubi.com"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testHttpURL() throws ValidationException
    {
        validator.validate(new FieldProvider("http://www.zutubi.com"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testEmptyURL() throws ValidationException
    {
        validator.validate(new FieldProvider(""));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testNullURL() throws ValidationException
    {
        validator.validate(new FieldProvider(null));
        assertFalse(validationAware.hasFieldErrors());
    }
}

