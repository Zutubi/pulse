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
        setDefaultMessageKey(".required");
    }

    public void validate(Object obj) throws ValidationException
    {
        Object fieldValue = getFieldValue(getFieldName(), obj);
        if (fieldValue == null)
        {
            addFieldError(getFieldName());
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
}
