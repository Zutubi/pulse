package com.zutubi.validation;

/**
 * <class-comment/>
 */
public interface FieldValidator extends Validator
{
    void setFieldName(String name);

    String getFieldName();
}
