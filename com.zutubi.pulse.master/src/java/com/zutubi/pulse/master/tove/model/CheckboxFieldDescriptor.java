package com.zutubi.pulse.master.tove.model;

import com.zutubi.pulse.master.tove.model.FieldDescriptor;

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
