package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

public class StringFieldValidatorSupportTest extends FieldValidatorTestCase
{
    @Override
    protected FieldValidator createValidator()
    {
        return new NoopStringFieldValidator();
    }

    public void testAllowEmpty() throws ValidationException
    {
        assertTrue(checkIsValid(true, null));
        assertTrue(checkIsValid(true, ""));
        assertTrue(checkIsValid(true, "something"));

        assertFalse(checkIsValid(false, null));
        assertFalse(checkIsValid(false, ""));
        assertTrue(checkIsValid(false, "something"));
    }

    private boolean checkIsValid(boolean allowEmpty, String value) throws ValidationException
    {
        validationAware.clearFieldErrors();

        setAllowEmpty(allowEmpty);
        validator.validate(new FieldProvider(value));
        return !validationAware.hasErrors();
    }

    private void setAllowEmpty(boolean b)
    {
        ((StringFieldValidatorSupport)validator).setAllowEmpty(b);
    }

    private class NoopStringFieldValidator extends StringFieldValidatorSupport
    {
        @Override
        protected void validateStringField(String value) throws ValidationException
        {
            // noop.
        }
    }

}
