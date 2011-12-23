package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.type.record.Record;

/**
 *
 *
 */
public class SubmitFieldDescriptor extends FieldDescriptor
{
    public static final String PARAM_DEFAULT = "default";

    public SubmitFieldDescriptor(boolean isDefault)
    {
        setType("submit");
        if(isDefault)
        {
            addParameter(PARAM_DEFAULT, true);
        }
    }

    public Field instantiate(String path, Record instance)
    {
        Field field = super.instantiate(path, instance);
        field.setValue(getName());
        return field;
    }
}
