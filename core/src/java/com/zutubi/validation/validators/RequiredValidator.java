package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.Shortcircuitable;

/**
 * <class-comment/>
 */
public class RequiredValidator extends FieldValidatorSupport implements Shortcircuitable
{
    public static final String REQUIRED = ".required";

    public RequiredValidator()
    {

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

    protected Object[] getMessageArgs()
    {
        return new Object[]{getFieldName()};
    }

    public String getMessageKey()
    {
        String messageKey = super.getMessageKey();
        if (messageKey != null)
        {
            return messageKey;
        }
        return getFieldName() + REQUIRED;
    }

    public String getDefaultMessage()
    {
        String defaultMessage = super.getDefaultMessage();
        if (defaultMessage != null)
        {
            return defaultMessage;
        }
        return validationContext.getText(REQUIRED, getMessageArgs());
    }
}
