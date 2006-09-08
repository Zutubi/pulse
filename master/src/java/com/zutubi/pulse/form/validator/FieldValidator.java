package com.zutubi.pulse.form.validator;

/**
 * <class-comment/>
 */
public interface FieldValidator extends Validator
{
    void setFieldName(String name);

    String getFieldName();
}
