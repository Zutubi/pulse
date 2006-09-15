package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ShortCircuitableValidator;

/**
 * <class-comment/>
 */
public class RequiredValidator extends FieldValidatorSupport
{
    public static final String REQUIRED = ".required";

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
            validationContext.addFieldError(getFieldName(), getMessage());
        }

        if (fieldValue instanceof String)
        {
            String str = ((String)fieldValue);
            if (str.length() == 0)
            {
                validationContext.addFieldError(getFieldName(), getMessage());
            }
        }
    }
}
