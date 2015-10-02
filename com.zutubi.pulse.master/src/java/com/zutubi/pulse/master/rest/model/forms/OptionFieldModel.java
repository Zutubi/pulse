package com.zutubi.pulse.master.rest.model.forms;

import java.util.List;

/**
 * Base class for fields that provide the user a set of options to choose from.
 */
public class OptionFieldModel extends FieldModel
{
    private Object emptyOption;
    private List list;
    private String listValue;
    private String listText;
    private boolean lazy;

    public OptionFieldModel()
    {
    }

    public OptionFieldModel(String type, String name, String label, List list)
    {
        super(type, name, label);
        this.list = list;
    }

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
}
