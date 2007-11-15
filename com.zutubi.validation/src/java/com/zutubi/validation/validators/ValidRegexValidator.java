package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * Validates that a string field can be compiled as a regular expression.
 */
public class ValidRegexValidator extends FieldValidatorSupport
{
    public void validate(Object obj) throws ValidationException
    {
        String fieldName = getFieldName();
        Object fieldValue = getFieldValue(fieldName, obj);

        if (fieldValue == null)
        {
            return;
        }

        if (!(fieldValue instanceof String))
        {
            throw new ValidationException();
        }

        String value = (String) fieldValue;
        if (value.length() == 0)
        {
            return;
        }


        try
        {
            Pattern.compile(value);
        }
        catch(PatternSyntaxException e)
        {
            validationContext.addFieldError(getFieldName(), e.getMessage());
        }
    }
}
