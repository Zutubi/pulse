package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;
import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.util.TextUtils;

/**
 *
 *
 */
public class IntegralValidator extends FieldValidatorSupport
{
    private boolean required = false;

    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        try
        {
            String str = (String) obj;
            if (!required && !TextUtils.stringSet(str))
            {
                return;
            }

            Long.parseLong(str);
        }
        catch (NumberFormatException e)
        {
            addFieldError(getFieldName(), e.getMessage());
        }
    }

    /**
     * An integral value is required.
     * 
     * @param required
     */
    public void setRequired(boolean required)
    {
        this.required = required;
    }
}
