package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Checks that a field has a valid email address.
 */
public class EmailValidator extends StringFieldValidatorSupport
{
    public void validateStringField(String str) throws ValidationException
    {
        try
        {
            new InternetAddress(str);
        }
        catch (AddressException e)
        {
            addError();
        }
    }
}
