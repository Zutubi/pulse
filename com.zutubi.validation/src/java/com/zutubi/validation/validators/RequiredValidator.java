package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * <class-comment/>
 */
public class RequiredValidator extends FieldValidatorSupport
{
    public static final String REQUIRED = ".required";
    private boolean ignorable = true;

    public RequiredValidator()
    {
        setMessageKey("${fieldName}.required");
        setDefaultMessageKey(".required");
    }

    public void validate(Object obj) throws ValidationException
    {
        Object fieldValue = getFieldValue(getFieldName(), obj);
        if (fieldValue == null)
        {
            addFieldError(getFieldName());
            return;
        }

        if (fieldValue instanceof String)
        {
            String str = ((String)fieldValue);
            if (str.length() == 0)
            {
                addFieldError(getFieldName());
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
