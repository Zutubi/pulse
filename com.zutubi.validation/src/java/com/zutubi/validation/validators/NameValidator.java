package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * Validates names used for entities in configuration.
 */
public class NameValidator extends StringFieldValidatorSupport
{
    public void validateStringField(String s) throws ValidationException
    {
        if(Character.isWhitespace(s.charAt(0)) || Character.isWhitespace(s.charAt(s.length() - 1)) || s.contains("/") || s.contains("\\") || s.contains("$"))
        {
            addError();
        }
    }
}
