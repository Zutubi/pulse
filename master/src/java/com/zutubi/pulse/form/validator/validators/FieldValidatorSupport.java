package com.zutubi.pulse.form.validator.validators;

import com.zutubi.pulse.form.validator.FieldValidator;

/**
 * <class-comment/>
 */
public abstract class FieldValidatorSupport extends ValidatorSupport implements FieldValidator
{
    private String fieldName;

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }
}
