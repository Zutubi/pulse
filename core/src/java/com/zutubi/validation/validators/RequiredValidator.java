package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;

/**
 * <class-comment/>
 */
public class RequiredValidator extends FieldValidatorSupport
{
    public static final String REQUIRED = ".required";

    public void validate(Object obj) throws ValidationException
    {
        Object fieldValue = getFieldValue(getFieldName(), obj);
        if (fieldValue == null)
        {
            validationContext.addFieldError(getFieldName(), getRequiredErrorText());
        }

        if (fieldValue instanceof String)
        {
            String str = ((String)fieldValue);
            if (str.length() == 0)
            {
                validationContext.addFieldError(getFieldName(), getRequiredErrorText());
            }
        }
    }

    private String getRequiredErrorText()
    {
        return validationContext.getText(getFieldName() + REQUIRED);
    }
}
