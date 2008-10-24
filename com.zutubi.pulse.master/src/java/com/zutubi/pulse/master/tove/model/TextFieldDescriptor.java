package com.zutubi.pulse.master.tove.model;

import com.zutubi.pulse.master.tove.model.FieldDescriptor;

/**
 *
 *
 */
public class TextFieldDescriptor extends FieldDescriptor
{
    public TextFieldDescriptor()
    {
        setType("text");
        setSubmitOnEnter(true);
    }

    public void setSize(int size)
    {
        addParameter("size", size);
    }
}
