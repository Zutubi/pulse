package com.cinnamonbob.xwork.validator.validators;

import com.opensymphony.xwork.validator.FieldValidator;

/**
 * <class-comment/>
 */
public class IntegralValidatorTest extends FieldValidatorTestBase
{
    public IntegralValidatorTest(String testName)
    {
        super(testName);
    }

    protected FieldValidator createValidator()
    {
        return new IntegralValidator();
    }

    public void testEmptyString() throws Exception
    {
        validator.validate(new FieldProvider(""));
        assertFalse(validationAware.hasErrors());
    }

    public void testEmptyStringWhenRequired() throws Exception
    {
        ((IntegralValidator)validator).setRequired(true);
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasErrors());
    }

    public void testAlphabeticString() throws Exception
    {
        validator.validate(new FieldProvider("a"));
        assertTrue(validationAware.hasErrors());
    }
}
