package com.zutubi.tove.validation;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorTestCase;

public class NameValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new NameValidator();
    }

    public void testDisallowTrailingSpace() throws ValidationException
    {
        validator.validate(new FieldProvider("blah "));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowTrailingTab() throws ValidationException
    {
        validator.validate(new FieldProvider("blah\t"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowLeadingSpace() throws ValidationException
    {
        validator.validate(new FieldProvider(" blah"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowLeadingTab() throws ValidationException
    {
        validator.validate(new FieldProvider("\tblah"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowForwardSlash() throws ValidationException
    {
        validator.validate(new FieldProvider("take a /"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowBackwardSlash() throws ValidationException
    {
        validator.validate(new FieldProvider("take a \\"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowDollar() throws ValidationException
    {
        validator.validate(new FieldProvider("take the $ and run"));
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

    public void testUnderscoreSeparated() throws ValidationException
    {
        validator.validate(new FieldProvider("a_a"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testBeginsWithUnderscore() throws ValidationException
    {
        validator.validate(new FieldProvider("_a"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testSingleUnderscore() throws ValidationException
    {
        validator.validate(new FieldProvider("_"));
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
