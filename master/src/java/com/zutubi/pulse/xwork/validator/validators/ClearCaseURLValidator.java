package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;
import com.opensymphony.xwork.validator.ValidationException;

/**
 * <class-comment/>
 */
public class ClearCaseURLValidator extends FieldValidatorSupport
{
    public void validate(Object object) throws ValidationException
    {
        String url = (String) getFieldValue(getFieldName(), object);
        if (!url.startsWith("clearcase:"))
        {
            addFieldError(getFieldName(), "url.invalid");
        }
    }
}
