package com.zutubi.tove.ui.model.tables;

/**
 * Represents a single column of a table.
 */
public class ColumnModel
{
    private String name;
    private String label;

    public ColumnModel(String name, String label)
    {
        this.name = name;
        this.label = label;
    }

    public String getName()
    {
        return name;
    }

    public String getLabel()
    {
        return label;
    }
}
