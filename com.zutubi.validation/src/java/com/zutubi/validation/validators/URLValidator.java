package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Checks for valid URLs.
 */
public class URLValidator extends StringFieldValidatorSupport
{
    public void validateStringField(String value) throws ValidationException
    {
        try
        {
            new URL(value);
        }
        catch (MalformedURLException e)
        {
            addError();
        }
    }

}
