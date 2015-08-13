package com.zutubi.pulse.master.rest.model.forms;

import com.zutubi.tove.annotations.FieldType;
import com.zutubi.tove.type.record.Record;

import java.util.List;

/**
 * Base class for fields that provide the user a set of options to choose from.
 */
public class OptionFieldModel extends FieldModel
{
    private boolean editable;
    private Object emptyOption;
    private List list;
    private String listValue;
    private String listText;
    private boolean multiple;
    private int size;
    private boolean lazy;

    /**
     * Indicates if the options for the field are provided lazily (i.e. only
     * when the user expands the field).
     *
     * @return true if the options for this field are provided lazily
     */
    public boolean isLazy()
    {
        return lazy;
    }

    public void setLazy(boolean lazy)
    {
        this.lazy = lazy;
    }

    public boolean isEditable()
    {
        return editable;
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }

    public Object getEmptyOption()
    {
        return emptyOption;
    }

    public void setEmptyOption(Object emptyOption)
    {
        this.emptyOption = emptyOption;
    }

    public List getList()
    {
        return list;
    }

    public void setList(List list)
    {
        this.list = list;
    }

    public String getListValue()
    {
        return listValue;
    }

    public void setListValue(String listValue)
    {
        this.listValue = listValue;
    }

    public String getListText()
    {
        return listText;
    }

    public void setListText(String listText)
    {
        this.listText = listText;
    }

    public boolean isMultiple()
    {
        return multiple;
    }

    public void setMultiple(boolean multiple)
    {
        this.multiple = multiple;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    // FIXME kendo this wont be called, we don't instantiate! either handle client side or late in form handling?
    public void instantiate(String path, Record instance)
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
    }

    protected boolean transformType()
    {
        return !isEditable() && getSize() <= 1;
    }
}
