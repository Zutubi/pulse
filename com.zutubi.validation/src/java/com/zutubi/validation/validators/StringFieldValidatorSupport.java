package com.zutubi.validation.validators;

import com.zutubi.util.StringUtils;
import com.zutubi.validation.ValidationException;

/**
 * Helper base for validation of string fields.  Also supports ignoring unset
 * string values.
 */
public abstract class StringFieldValidatorSupport extends FieldValidatorSupport
{
    private boolean allowEmpty = false;

    protected StringFieldValidatorSupport()
    {
    }

    protected StringFieldValidatorSupport(String defaultKeySuffix)
    {
        super(defaultKeySuffix);
    }

    protected StringFieldValidatorSupport(boolean allowEmpty)
    {
        this.allowEmpty = allowEmpty;
    }

    protected StringFieldValidatorSupport(String defaultKeySuffix, boolean allowEmpty)
    {
        super(defaultKeySuffix);
        this.allowEmpty = allowEmpty;
    }

    public void setAllowEmpty(boolean allowEmpty)
    {
        this.allowEmpty = allowEmpty;
    }

    protected void validateField(Object value) throws ValidationException
    {
        if (value == null)
        {
            if (allowEmpty)
            {
                return;
            }
        }
        else if(!(value instanceof String))
        {
            throw new ValidationException("Expecting string value for field '" + getFieldLabel() + "'");
        }

        String s = (String) value;

        if (!StringUtils.stringSet(s) && !allowEmpty)
        {
            addError(".required");
            return;
        }

        if (StringUtils.stringSet(s))
        {
            validateStringField(s);
        }
    }

    protected abstract void validateStringField(String value) throws ValidationException;
}
