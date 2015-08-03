package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;

/**
 * Models a simple text entry field.
 */
public class TextFieldModel extends FieldModel
{
    private int size;

    public TextFieldModel()
    {
        setType(FieldType.TEXT);
        setSubmitOnEnter(true);
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }
}
