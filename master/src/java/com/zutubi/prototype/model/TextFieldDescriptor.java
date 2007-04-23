package com.zutubi.prototype.model;

import com.zutubi.prototype.FieldDescriptor;

/**
 *
 *
 */
public class TextFieldDescriptor extends FieldDescriptor
{
    public TextFieldDescriptor()
    {
        setType("text");
    }

    public void setSize(int size)
    {
        addParameter("size", size);
    }
}
