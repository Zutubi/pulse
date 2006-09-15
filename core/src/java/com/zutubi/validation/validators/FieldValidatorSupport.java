package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ShortCircuitableValidator;

/**
 * <class-comment/>
 */
public abstract class FieldValidatorSupport extends ValidatorSupport implements FieldValidator, ShortCircuitableValidator
{
    private String fieldName;

    private ValidationContext context;

    private String defaultMessage;

    private String messageKey;
    private String defaultMessageKey;

    private boolean shortCircuit = true;

    public void setShortCircuit(boolean b)
    {
        shortCircuit = b;
    }

    public boolean isShortCircuit()
    {
        return shortCircuit;
    }

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
        return new Object[]{getFieldName()};
    }

    protected String getMessage()
    {
        // just a bit of craziness...
        String message;

        if (messageKey != null)
        {
            messageKey = messageKey.replace("${fieldName}", getFieldName());
            message = validationContext.getText(messageKey);
            if (message == null)
            {
                message = determineDefaultMessage();
            }
        }
        else
        {
            message = determineDefaultMessage();
        }

        if (message == null)
        {
            message = "no.message.available";
        }
        return message;
    }

    private String determineDefaultMessage()
    {
        if (defaultMessage != null)
        {
            return defaultMessage;
        }
        else
        {
            if (defaultMessageKey != null)
            {
                defaultMessageKey = defaultMessageKey.replace("${fieldName}", getFieldName());
                defaultMessage = validationContext.getText(defaultMessageKey);
            }
            if (defaultMessage == null)
            {
                defaultMessage = messageKey;
            }
            return defaultMessage;
        }
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

    protected void setDefaultMessageKey(String messageKey)
    {
        this.defaultMessageKey = messageKey;
    }
}
