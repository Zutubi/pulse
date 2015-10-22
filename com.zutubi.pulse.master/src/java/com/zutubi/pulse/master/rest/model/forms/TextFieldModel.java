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
        this(null, null);
    }

    public TextFieldModel(String name, String label)
    {
        super(FieldType.TEXT, name, label);
        setType(FieldType.TEXT);
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
