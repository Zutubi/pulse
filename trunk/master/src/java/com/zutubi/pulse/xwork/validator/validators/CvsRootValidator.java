package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;
import org.netbeans.lib.cvsclient.CVSRoot;

/**
 *
 *
 */
public class CvsRootValidator extends FieldValidatorSupport
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
            CVSRoot.parse((String) obj);
        }
        catch (IllegalArgumentException iae)
        {
            setDefaultMessage(iae.getMessage());
            addFieldError(getFieldName(), object);
        }
    }
}
