package com.cinnamonbob.xwork.validator.validators;

import com.opensymphony.xwork.validator.FieldValidator;

/**
 * <class-comment/>
 */
public class CronExpressionValidatorTest extends FieldValidatorTestBase
{

    public CronExpressionValidatorTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

    }

    public void tearDown() throws Exception
    {

        super.tearDown();
    }

    protected FieldValidator createValidator()
    {
        return new CronExpressionValidator();
    }

    public void testEmptyString() throws Exception
    {
        validator.validate(new FieldProvider(""));
        assertFalse(validationAware.hasErrors());
    }

    public void testNull() throws Exception
    {
        validator.validate(new FieldProvider(""));
        assertFalse(validationAware.hasErrors());
    }
}