package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * Validates names used for entities in configuration.
 */
public class NameValidator extends RegexValidator
{
    public NameValidator()
    {
        setDefaultMessageKey(".invalid");
        setMessageKey("${fieldName}.invalid");
    }

    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        if (obj == null || !(obj instanceof String))
        {
            return;
        }

        String s = (String) obj;
        if(s.length() > 0)
        {
            if(Character.isWhitespace(s.charAt(0)) || Character.isWhitespace(s.charAt(s.length() - 1)) || s.contains("/") || s.contains("\\") || s.contains("$"))
            {
                addFieldError(getFieldName());
            }
        }
    }
}
