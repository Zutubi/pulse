package com.zutubi.prototype.model;

import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.type.record.Record;

import java.util.List;

/**
 */
public class OptionFieldDescriptor extends FieldDescriptor
{
    private static final String PARAMETER_EMPTY_OPTION = "emptyOption";
    private static final String PARAMETER_LIST = "list";
    private static final String PARAMETER_MULTIPLE = "multiple";
    private static final String PARAMETER_SIZE = "size";

    public Object getEmptyOption()
    {
        return getParameter(PARAMETER_EMPTY_OPTION);
    }

    public void setEmptyOption(Object option)
    {
        addParameter(PARAMETER_EMPTY_OPTION, option);
    }

    public List getList()
    {
        return (List) getParameter(PARAMETER_LIST);
    }

    public void setList(List list)
    {
        addParameter(PARAMETER_LIST, list);
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
