package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

/**
 *
 *
 */
public class PatternGroupValidatorTest extends FieldValidatorTestCase
{
    public PatternGroupValidatorTest(String testName)
    {
        super(testName);
    }

    protected FieldValidator createValidator()
    {
        return new PatternGroupValidator();
    }

    public void testInvalidGroupReference() throws ValidationException
    {
        validator.validate(new FieldProvider("${1}"));
        assertTrue(validationAware.hasErrors());
    }

    public void testValidGroupReference() throws ValidationException
    {
        validator.validate(new FieldProvider("$1"));
        assertFalse(validationAware.hasErrors());
    }

    public void testMultipleGroupReferences() throws ValidationException
    {
        validator.validate(new FieldProvider("$1$1$3$5"));
        assertFalse(validationAware.hasErrors());
    }

    public void testAllAvailableGroupReferences() throws ValidationException
    {
        validator.validate(new FieldProvider("$1$2$3$4$5$6$7$8$9"));
        assertFalse(validationAware.hasErrors());
    }

}
