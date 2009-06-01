package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

import java.util.Collection;

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
        if (!isValueSet(fieldValue))
        {
            addError();
        }
    }

    public static boolean isValueSet(Object fieldValue)
    {
        if (fieldValue == null)
        {
            return false;
        }

        if (fieldValue instanceof String)
        {
            String str = ((String)fieldValue);
            if (str.length() == 0)
            {
                return false;
            }
        }

        if (fieldValue instanceof Collection)
        {
            Collection c = (Collection) fieldValue;
            if (c.size() == 0)
            {
                return false;
            }
        }

        return true;
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
