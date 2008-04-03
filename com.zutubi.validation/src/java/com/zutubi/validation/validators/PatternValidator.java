package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Validates that a string field can be compiled as a regular expression.
 */
public class PatternValidator extends StringFieldValidatorSupport
{
    public void validateStringField(String str) throws ValidationException
    {
        try
        {
            Pattern.compile(str);
        }
        catch (PatternSyntaxException e)
        {
            addErrorMessage(e.getMessage());
        }
    }
}
