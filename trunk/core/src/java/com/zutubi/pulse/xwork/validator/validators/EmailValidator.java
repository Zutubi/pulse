package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * <class comment/>
 */
public class EmailValidator extends FieldValidatorSupport
{
    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        if (obj == null || !(obj instanceof String))
        {
            addFieldError(getFieldName(), "A string value is required.");
            return;
        }
        try
        {
            new InternetAddress((String)obj).validate();
        }
        catch (AddressException e)
        {
            setDefaultMessage(e.getMessage());
            addFieldError(getFieldName(), object);
        }
    }
}
