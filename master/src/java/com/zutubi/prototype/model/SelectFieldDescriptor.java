package com.zutubi.prototype.model;

import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.type.record.Record;

import java.util.Collection;

/**
 *
 *
 */
public class SelectFieldDescriptor extends FieldDescriptor
{
    private static final String PARAMETER_MULTIPLE = "multiple";
    private static final String PARAMETER_SIZE = "size";

    public SelectFieldDescriptor()
    {
        setType("select");
    }

    public Collection getList()
    {
        return (Collection) getParameter("list");
    }

    public void setList(Collection list)
    {
        addParameter("list", list);
    }

    public void setListKey(String listKey)
    {
        addParameter("listKey", listKey);
    }

    public void setListValue(String listValue)
    {
        addParameter("listValue", listValue);
    }

    public boolean getMultiple()
    {
        return (Boolean) getParameter(PARAMETER_MULTIPLE, false);
    }

    public void setMultiple(boolean multiple)
    {
        addParameter(PARAMETER_MULTIPLE, multiple);
    }

    public int getSize()
    {
        return (Integer) getParameter(PARAMETER_SIZE);
    }

    public void setSize(int size)
    {
        addParameter(PARAMETER_SIZE, size);
    }

    public Field instantiate(String path, Record instance)
    {
        if(!getMultiple() && (!hasParameter(PARAMETER_SIZE) || getSize() == 1))
        {
            setType("combobox");
        }

        return super.instantiate(path, instance);
    }
}
