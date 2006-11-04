package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.FieldValidator;

/**
 * <class comment/>
 */
public class EmailValidatorTest extends FieldValidatorTestBase
{
    public EmailValidatorTest(String testName)
    {
        super(testName);
    }

    protected FieldValidator createValidator()
    {
        return new EmailValidator();
    }

    public void testValidEmail() throws Exception
    {
        validator.validate(new FieldProvider("daniel@localhost.com"));
        assertFalse(validationAware.hasErrors());
    }

    public void testValidEmailWithPersonalDetails() throws Exception
    {
        validator.validate(new FieldProvider("Daniel <daniel@localhost.com>"));
        assertFalse(validationAware.hasErrors());
    }

    public void testInValidEmail() throws Exception
    {
        validator.validate(new FieldProvider("dan iel@localhost.com"));
        assertTrue(validationAware.hasErrors());
    }
}
