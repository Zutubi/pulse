package com.zutubi.prototype.table;

/**
 *
 *
 */
public class ColumnDescriptor
{
    private String name;

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

    public void setName(String name)
    {
        this.name = name;
    }
}
