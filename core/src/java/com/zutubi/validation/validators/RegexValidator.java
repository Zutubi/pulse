package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

/**
 * <class-comment/>
 */
public class RegexValidator extends FieldValidatorSupport
{
    private static final String INVALID = ".invalid";

    private String pattern;

    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public void validate(Object obj) throws ValidationException
    {
        String fieldName = getFieldName();
        Object value = getFieldValue(fieldName, obj);
        if (value == null)
        {
            return;
        }
        if (!(value instanceof String))
        {
            throw new ValidationException();
        }

        String stringValue = (String) value;
        if (stringValue.length() > 0 && !stringValue.matches(pattern))
        {
            validationContext.addFieldError(getFieldName(), getMessage());
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
        return getFieldName() + INVALID;
    }

    public String getDefaultMessage()
    {
        String defaultMessage = super.getDefaultMessage();
        if (defaultMessage != null)
        {
            return defaultMessage;
        }
        return validationContext.getText(INVALID, getMessageArgs());
    }
}
