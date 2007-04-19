package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <class comment/>
 */
public class PatternValidator extends FieldValidatorSupport
{
    public PatternValidator()
    {
        setDefaultMessageKey(".invalid");
        setMessageKey("${fieldName}.invalid");
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
            String str = ((String) fieldValue);

            try
            {
                Pattern.compile(str);
            }
            catch (PatternSyntaxException e)
            {
                setDefaultMessage(getDefaultMessage());
                validationContext.addFieldError(getFieldName(), e.getMessage());
            }
        }
    }
}
