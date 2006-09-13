package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class RequiredValidatorTest extends FieldValidatorTestCase
{
    public RequiredValidatorTest(String testName)
    {
        super(testName);
    }

    protected FieldValidator createValidator()
    {
        return new RequiredValidator();
    }

    public void testNullObject() throws ValidationException
    {
        validator.validate(new FieldProvider(null));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("field.required"), validationAware.getFieldErrors("field"));
    }

    public void testObject() throws ValidationException
    {
        validator.validate(new FieldProvider(new Object()));
        assertFalse(validationAware.hasErrors());
    }

    public void testEmptyString() throws ValidationException
    {
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("field.required"), validationAware.getFieldErrors("field"));
    }

    public void testSomeString() throws ValidationException
    {
        validator.validate(new FieldProvider("asdfasf"));
        assertFalse(validationAware.hasErrors());
    }
}
