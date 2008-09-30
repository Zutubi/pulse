package com.zutubi.tove.model;

import com.zutubi.tove.FieldDescriptor;

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
