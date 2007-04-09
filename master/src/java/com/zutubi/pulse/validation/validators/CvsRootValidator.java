package com.zutubi.pulse.validation.validators;

import org.netbeans.lib.cvsclient.CVSRoot;
import com.zutubi.validation.validators.FieldValidatorSupport;
import com.zutubi.validation.ValidationException;

/**
 *
 *
 */
public class CvsRootValidator extends FieldValidatorSupport
{
    public CvsRootValidator()
    {
        setDefaultMessageKey(".invalid");
        setMessageKey("${fieldName}.invalid");
    }

    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        try
        {
            CVSRoot.parse((String) obj);
        }
        catch (IllegalArgumentException iae)
        {
            setDefaultMessage(iae.getMessage());
            addFieldError(getFieldName());
        }
    }
}
