package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class NumericValidatorTest extends FieldValidatorTestCase
{
    public NumericValidatorTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        
        textProvider.addText("field.min", "field.min");
        textProvider.addText("field.max", "field.max");
    }

    protected FieldValidator createValidator()
    {
        return new NumericValidator();
    }

    public void testMinValidation() throws ValidationException
    {
        ((NumericValidator)validator).setMin(5);
        validator.validate(new FieldProvider(3));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("field.min"), validationAware.getFieldErrors("field"));
    }

    public void testMaxValidation() throws ValidationException
    {
        ((NumericValidator)validator).setMax(2);
        validator.validate(new FieldProvider(3));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("field.max"), validationAware.getFieldErrors("field"));
    }

    public void testMinValidationLong() throws ValidationException
    {
        ((NumericValidator)validator).setMin(5);
        validator.validate(new FieldProvider((long) 3));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("field.min"), validationAware.getFieldErrors("field"));
    }

    public void testMaxValidationLong() throws ValidationException
    {
        ((NumericValidator)validator).setMax(2);
        validator.validate(new FieldProvider((long) 3));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("field.max"), validationAware.getFieldErrors("field"));
    }

    public void testUnsetInt() throws ValidationException
    {
        ((NumericValidator)validator).setMin(0);
        validator.validate(new FieldProvider(Integer.MIN_VALUE));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testUnsetLong() throws ValidationException
    {
        ((NumericValidator)validator).setMin(0);
        validator.validate(new FieldProvider(Long.MIN_VALUE));
        assertFalse(validationAware.hasFieldErrors());
    }
}
