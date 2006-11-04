package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * <class-comment/>
 */
public class EmailValidator extends FieldValidatorSupport
{
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
                new InternetAddress(str);
            }
            catch (AddressException e)
            {
                addFieldError(getFieldName());
            }
        }

    }
}