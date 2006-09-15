package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.i18n.TextProvider;

/**
 * <class-comment/>
 */
public abstract class FieldValidatorSupport extends ValidatorSupport implements FieldValidator
{
    private String fieldName;

    private ValidationContext context;

    private String defaultMessage;

    private String messageKey;

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    protected Object[] getMessageArgs()
    {
        return new Object[]{};
    }

    protected String getMessage()
    {
        // first, check if the fieldName.required text is available.
        String message = validationContext.getText(getMessageKey(), getDefaultMessage(), getMessageArgs());
        if (message != null)
        {
            return message;
        }
        if (getMessageKey() != null)
        {
            return getMessageKey();
        }
        return "no.message.available";
    }

    public String getDefaultMessage()
    {
        return defaultMessage;
    }

    public void setDefaultMessage(String defaultMessage)
    {
        this.defaultMessage = defaultMessage;
    }

    public String getMessageKey()
    {
        return messageKey;
    }

    public void setMessageKey(String messageKey)
    {
        this.messageKey = messageKey;
    }
}
