package com.zutubi.prototype.model;

import com.zutubi.prototype.FieldDescriptor;

/**
 *
 *
 */
public class CheckboxFieldDescriptor extends FieldDescriptor
{
    public CheckboxFieldDescriptor()
    {
        setType("checkbox");
        setSubmitOnEnter(true);
    }
}
