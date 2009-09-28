package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

public class PatternValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new PatternValidator();
    }

    public void testRegex() throws ValidationException
    {
        validator.validate(new FieldProvider(".*"));
        assertFalse(validationAware.hasErrors());
    }

    public void testNumeralsOnly() throws ValidationException
    {
        validator.validate(new FieldProvider("[0-9]"));
        assertFalse(validationAware.hasErrors());
    }

    public void testInvalidRegex() throws ValidationException
    {
        validator.validate(new FieldProvider("["));
        assertTrue(validationAware.hasErrors());
    }
}
