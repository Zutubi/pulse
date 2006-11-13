package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

/**
 * <class-comment/>
 */
public class IntegerRangeValidatorTest extends FieldValidatorTestCase
{
    public IntegerRangeValidatorTest(String testName)
    {
        super(testName);
    }

    protected FieldValidator createValidator()
    {
        return new IntegerRangeValidator();
    }

    public void setUp() throws Exception
    {
        super.setUp();

        ((IntegerRangeValidator)validator).setMin(5);
        ((IntegerRangeValidator)validator).setMax(10);
    }

    public void testValidRange() throws ValidationException
    {
        validator.validate(new FieldProvider(7));
        assertFalse(validationAware.hasErrors());
    }

    public void testBelowRange() throws ValidationException
    {
        validator.validate(new FieldProvider(2));
        assertTrue(validationAware.hasErrors());
    }

    public void testAboveRange() throws ValidationException
    {
        validator.validate(new FieldProvider(11));
        assertTrue(validationAware.hasErrors());
    }

    public void testUpperRangeBoundry() throws ValidationException
    {
        validator.validate(new FieldProvider(10));
        assertFalse(validationAware.hasErrors());
    }

    public void testLowerRangeBoundry() throws ValidationException
    {
        validator.validate(new FieldProvider(5));
        assertFalse(validationAware.hasErrors());
    }
}
