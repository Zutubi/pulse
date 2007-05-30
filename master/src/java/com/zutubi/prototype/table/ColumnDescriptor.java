package com.zutubi.prototype.table;

/**
 * The column descriptor represents the model used to render a column to the UI.
 *
 */
public class ColumnDescriptor
{
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

    public ColumnDescriptor()
    {
    }

    public ColumnDescriptor(String name)
    {
        this.name = name;
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
}
