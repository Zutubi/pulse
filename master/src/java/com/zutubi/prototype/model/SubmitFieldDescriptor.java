package com.zutubi.prototype.model;

import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.type.record.Record;

/**
 *
 *
 */
public class SubmitFieldDescriptor extends FieldDescriptor
{
    public SubmitFieldDescriptor(boolean isDefault)
    {
        setType("submit");
        if(isDefault)
        {
            addParameter("default", true);
        }
    }

    public Field instantiate(String path, Record instance)
    {
        Field field = super.instantiate(path, instance);
        field.setValue(getName());
        return field;
    }
}
