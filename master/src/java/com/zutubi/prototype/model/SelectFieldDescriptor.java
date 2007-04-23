package com.zutubi.prototype.model;

import com.zutubi.prototype.FieldDescriptor;

import java.util.Collection;

/**
 *
 *
 */
public class SelectFieldDescriptor extends FieldDescriptor
{
    public SelectFieldDescriptor()
    {
        setType("select");
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

    public void setMultiple(boolean multiple)
    {
        addParameter("multiple", multiple);
    }
}
