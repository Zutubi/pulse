package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * <class-comment/>
 */
public class EmailValidator extends FieldValidatorSupport
{
    public EmailValidator()
    {
        setDefaultMessageKey(".invalid");
        setMessageKey("${fieldName}.invalid");
    }

    public void validate(Object obj) throws ValidationException
    {
        Object fieldValue = getFieldValue(getFieldName(), obj);
        if (fieldValue == null)
        {
            return;
        }

        if (fieldValue instanceof String)
        {
            String str = ((String) fieldValue);

            if (str.length() > 0)
            {
                try
                {
                    new InternetAddress(str);
                }
                catch (AddressException e)
                {
                    setDefaultMessage(getDefaultMessage());
                    addFieldError(getFieldName());
                }
            }
        }

    }
}