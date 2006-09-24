package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

/**
 * <class-comment/>
 */
public class EmailValidatorTest extends FieldValidatorTestCase
{
    public EmailValidatorTest(String testName)
    {
        super(testName);
    }

    protected FieldValidator createValidator()
    {
        return new EmailValidator();
    }

    public void testValidEmail() throws ValidationException
    {
        validator.validate(new FieldProvider("support@zutubi.com"));
        assertFalse(validationAware.hasErrors());
    }

    public void testInvalidEmail() throws ValidationException
    {
        validator.validate(new FieldProvider("no support @zutubi.com"));
        assertTrue(validationAware.hasErrors());
    }
}
