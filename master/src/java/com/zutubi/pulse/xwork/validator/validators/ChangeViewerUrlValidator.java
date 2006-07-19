package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;
import com.zutubi.pulse.model.Scm;

/**
 *
 *
 */
public class ChangeViewerUrlValidator extends FieldValidatorSupport
{
    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        if (obj != null)
        {
            if(obj instanceof String)
            {
                try
                {
                    Scm.validateChangeViewerURL((String) obj);
                }
                catch (IllegalArgumentException iae)
                {
                    setDefaultMessage(iae.getMessage());
                    addFieldError(getFieldName(), object);
                }
            }
            else
            {
                addFieldError(getFieldName(), "A string value is required.");
            }
        }
    }
}
