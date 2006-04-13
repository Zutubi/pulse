/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;

/**
 *
 *
 */
public class NameValidator extends FieldValidatorSupport
{
    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        if (obj != null)
        {
            String value = (String) obj;
            if (value.length() > 0 && !value.matches("[a-zA-Z0-9][-a-zA-Z0-9_. ]*"))
            {
                addFieldError(getFieldName(), obj);
            }
        }
    }
}
