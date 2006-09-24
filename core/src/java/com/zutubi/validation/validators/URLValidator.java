package com.zutubi.validation.validators;

import com.zutubi.validation.ValidationException;
import com.opensymphony.util.TextUtils;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * <class-comment/>
 */
public class URLValidator extends FieldValidatorSupport
{
    public void validate(Object obj) throws ValidationException
    {
        String fieldName = getFieldName();
        Object value = this.getFieldValue(fieldName, obj);

        if (value == null || value.toString().length() == 0)
        {
            return;
        }

        if (!(value.getClass().equals(String.class)) || !verifyUrl((String) value))
        {
            validationContext.addFieldError(fieldName, (String) obj);
        }
    }

    private boolean verifyUrl(String url)
    {
        if (url == null)
        {
            return false;
        }

        try
        {
            new URL(url);

            return true;
        }
        catch (MalformedURLException e)
        {
            return false;
        }
    }
}
