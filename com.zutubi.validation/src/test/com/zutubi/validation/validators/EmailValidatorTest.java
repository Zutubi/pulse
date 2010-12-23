package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

public class EmailValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new EmailValidator();
    }

    public void testValidEmail() throws ValidationException
    {
        validator.validate(new FieldProvider("support@zutubi.com"));
        assertFalse(validationAware.hasErrors());
    }

    public void testValidEmailWithPersonalDetails() throws ValidationException
    {
        validator.validate(new FieldProvider("Support <support@zutubi.com>"));
        assertFalse(validationAware.hasErrors());
    }

    public void testInvalidEmail() throws ValidationException
    {
        validator.validate(new FieldProvider("no support @zutubi.com"));
        assertTrue(validationAware.hasErrors());
    }

    public void testDoNotAllowEmpty() throws ValidationException
    {
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasErrors());
    }

    public void testDoNotAllowNull() throws ValidationException
    {
        validator.validate(new FieldProvider(null));
        assertTrue(validationAware.hasErrors());
    }
}
