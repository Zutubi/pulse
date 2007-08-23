package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

/**
 *
 *
 */
public class NameValidatorTest extends FieldValidatorTestCase
{
    public NameValidatorTest(String testName)
    {
        super(testName);
    }

    protected FieldValidator createValidator()
    {
        return new NameValidator();
    }

    public void testDisallowTrailingSpace() throws ValidationException
    {
        validator.validate(new FieldProvider("blah "));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowLeadingSpace() throws ValidationException
    {
        validator.validate(new FieldProvider(" blah"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testSingleCharacterName() throws ValidationException
    {
        validator.validate(new FieldProvider("b"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testMultipleCharacterWord() throws ValidationException
    {
        validator.validate(new FieldProvider("blah"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testAllowMultipleWords() throws ValidationException
    {
        validator.validate(new FieldProvider("blah blah blah"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testAllowMultipleSingleCharacterWords() throws ValidationException
    {
        validator.validate(new FieldProvider("b b b"));
        assertFalse(validationAware.hasFieldErrors());
    }
}
