package com.zutubi.pulse.core.scm.cvs.validation.validators;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.StringFieldValidatorSupport;
import org.netbeans.lib.cvsclient.CVSRoot;

/**
 */
public class CvsRootValidator extends StringFieldValidatorSupport
{
    public void validateStringField(String value) throws ValidationException
    {
        try
        {
            CVSRoot.parse(value);
        }
        catch (IllegalArgumentException iae)
        {
            addError(iae.getMessage());
        }
    }
}
