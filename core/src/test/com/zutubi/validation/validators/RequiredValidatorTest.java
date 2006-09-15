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

    public void testEmptyString() throws ValidationException
    {
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasFieldErrors());

        assertEquals(Arrays.asList("field.required"), validationAware.getFieldErrors("field"));
    }

    public void testObject() throws ValidationException
    {
        validator.validate(new FieldProvider(new Object()));
        assertFalse(validationAware.hasErrors());
    }

    public void testSomeString() throws ValidationException
    {
        validator.validate(new FieldProvider("asdfasf"));
        assertFalse(validationAware.hasErrors());
    }

    public void testDefaultErrorMessageKey() throws ValidationException
    {
        textProvider.addText(".required", "Required Field");
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("Required Field"), validationAware.getFieldErrors("field"));
    }

    public void testDefaultErrorMessage() throws ValidationException
    {
        textProvider.addText(".required", "Required Field");
        ((RequiredValidator)validator).setDefaultMessage("Default Message");
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("Default Message"), validationAware.getFieldErrors("field"));
    }

    public void testErrorMessage() throws ValidationException
    {
        textProvider.addText(".required", "Required Field");
        textProvider.addText("field.required", "Field is Required");
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("Field is Required"), validationAware.getFieldErrors("field"));
    }
}
