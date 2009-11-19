package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.type.record.Record;

import java.util.List;

/**
 */
public class OptionFieldDescriptor extends FieldDescriptor
{
    public static final String PARAMETER_EDITABLE = "editable";
    public static final String PARAMETER_EMPTY_OPTION = "emptyOption";
    public static final String PARAMETER_LIST = "list";
    public static final String PARAMETER_MULTIPLE = "multiple";
    public static final String PARAMETER_SIZE = "size";

    /**
     * Indicates if the options for the field are provided lazily (i.e. only
     * when the user expands the field).
     *
     * @return true if the options for this field are provided lazily
     */
    public boolean isLazy()
    {
        return false;
    }
    
    public boolean isEditable()
    {
        return getParameter(PARAMETER_EDITABLE, false);
    }

    public void setEditable(boolean editable)
    {
        addParameter(PARAMETER_EDITABLE, editable);
    }
    
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
        return getParameter(PARAMETER_MULTIPLE, false);
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
        if (transformType())
        {
            if (isEditable())
            {
                setType(FieldType.COMBOBOX);
            }
            else
            {
                setType(FieldType.DROPDOWN);
            }
        }

        return super.instantiate(path, instance);
    }

    protected boolean transformType()
    {
        return !getMultiple() && (!hasParameter(PARAMETER_SIZE) || getSize() == 1);
    }
}
