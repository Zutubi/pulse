package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * Used to ensure a field has a non-empty value.
 */
public class RequiredValidator extends FieldValidatorSupport
{
    private boolean ignorable = true;

    public RequiredValidator()
    {
        super("required");
    }

    public void validateField(Object fieldValue) throws ValidationException
    {
        if (fieldValue == null)
        {
            addError();
            return;
        }

        if (fieldValue instanceof String)
        {
            String str = ((String)fieldValue);
            if (str.length() == 0)
            {
                addError();
            }
        }
    }

    public boolean isIgnorable()
    {
        return ignorable;
    }

    public void setIgnorable(boolean ignorable)
    {
        this.ignorable = ignorable;
    }
}
