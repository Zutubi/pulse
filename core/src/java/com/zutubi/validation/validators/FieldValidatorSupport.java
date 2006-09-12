package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationContext;

/**
 * <class-comment/>
 */
public abstract class FieldValidatorSupport extends ValidatorSupport implements FieldValidator
{
    private String fieldName;

    private ValidationContext context;

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }
}
