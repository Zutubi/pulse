package com.zutubi.pulse.form.validator.validators;

import com.zutubi.pulse.form.validator.ValidationException;

/**
 * <class-comment/>
 */
public class RequiredStringFieldValidator extends RequiredFieldValidator
{
    public void validate(Object obj) throws ValidationException
    {
        super.validate(obj);

        if (hasErrors())
        {
            return;
        }

        Object value = getFieldValue(getFieldName(), obj);
        if (!(value instanceof String))
        {
            throw new ValidationException("Attempting to apply a string validator to a non string field '" +
                    getFieldName() + "'");
        }

        // ensure that we have a string of length greater than zero.
        String str = (String) value;
        if (str.length() == 0)
        {
            addFieldError(getFieldName(), getFieldName() + REQUIRED);
        }
    }
}
