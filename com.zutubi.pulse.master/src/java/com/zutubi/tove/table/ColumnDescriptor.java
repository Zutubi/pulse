package com.zutubi.tove.table;

import com.zutubi.tove.type.CompositeType;
import com.zutubi.util.bean.ObjectFactory;

/**
 * The column descriptor represents the model used to render a column to the UI.
 */
public class ColumnDescriptor
{
    private ObjectFactory objectFactory;

    /**
     * The name of this column.
     */
    private String name;

    /**
     * Indicate whether or not this column is sortable.  This is an optional setting that does not need to
     * be supported by the UI. 
     */
    private boolean sortable;

    /**
     * Indicate whether or not this column can be removed from display.  This is an optional setting that does not need
     * to be supported by the UI.
     */
    private boolean required;

    private CompositeType type;

    public ColumnDescriptor()
    {
    }

    public ColumnDescriptor(String name)
    {
        this.name = name;
    }

    public void setType(CompositeType type)
    {
        this.type = type;
    }

    public Object getValue(Object instance)
    {
        try
        {
            FormattingWrapper wrapper = objectFactory.buildBean(FormattingWrapper.class, new Class[]{Object.class, CompositeType.class}, new Object[]{instance, type});
            Object value = wrapper.get(name);
            if (value != null)
            {
                return value;
            }
            return "";
        }
        catch (Exception e)
        {
            return "";
        }
    }

    public String getName()
    {
        return name;
    }

    public boolean isSortable()
    {
        return sortable;
    }

    public void setSortable(boolean sortable)
    {
        this.sortable = sortable;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
