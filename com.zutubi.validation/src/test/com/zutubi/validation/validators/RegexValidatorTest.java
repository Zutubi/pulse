package com.zutubi.validation.validators;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

/**
 * <class-comment/>
 */
public class RegexValidatorTest extends FieldValidatorTestCase
{
    public RegexValidatorTest(String testName)
    {
        super(testName);
    }

    protected FieldValidator createValidator()
    {
        return new RegexValidator();
    }

    public void testRegex() throws ValidationException
    {
        ((RegexValidator)validator).setPattern(".*");
        validator.validate(new FieldProvider("blah"));
        assertFalse(validationAware.hasErrors());
    }

    public void testNumeralsOnly() throws ValidationException
    {
        ((RegexValidator)validator).setPattern("[0-9]");
        validator.validate(new FieldProvider("blah"));
        assertTrue(validationAware.hasErrors());
    }
}
