package com.zutubi.tove.model;

import com.zutubi.tove.FieldDescriptor;

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
