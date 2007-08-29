package com.zutubi.prototype.model;

import com.zutubi.prototype.FieldDescriptor;

import java.util.List;

/**
 *
 *
 */
public class StringListFieldDescriptor extends FieldDescriptor
{
    private static final String PARAMETER_LIST = "list";
    private static final String PARAMETER_SIZE = "size";

    public StringListFieldDescriptor()
    {
        setType("stringlist");
    }

    public List getList()
    {
        return (List) getParameter(PARAMETER_LIST);
    }

    public void setList(List list)
    {
        addParameter(PARAMETER_LIST, list);
    }

    public int getSize()
    {
        return (Integer) getParameter(PARAMETER_SIZE);
    }

    public void setSize(int size)
    {
        addParameter(PARAMETER_SIZE, size);
    }
}
