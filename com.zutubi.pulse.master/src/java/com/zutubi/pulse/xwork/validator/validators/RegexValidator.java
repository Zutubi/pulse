package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;

import java.util.regex.Pattern;

/**
 */
public class RegexValidator extends FieldValidatorSupport
{
    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        if (obj != null && obj instanceof String)
        {
            String expression = (String) obj;

            if (expression.length() > 0)
            {
                try
                {
                    Pattern.compile(expression);
                }
                catch (Exception e)
                {
                    setDefaultMessage(e.getMessage());
                    addFieldError(getFieldName(), object);
                }
            }
        }
    }
}
