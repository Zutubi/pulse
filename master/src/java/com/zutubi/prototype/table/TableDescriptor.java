package com.zutubi.prototype.table;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class TableDescriptor
{
    private List<ColumnDescriptor> columns = new LinkedList<ColumnDescriptor>();

    private List<String> actions = new LinkedList<String>();

    public List<ColumnDescriptor> getColumns()
    {
        return new LinkedList<ColumnDescriptor>(columns);
    }

    public void add(ColumnDescriptor descriptor)
    {
        columns.add(descriptor);
    }

    public List<String> getActions()
    {
        return actions;
    }

    public void add(String action)
    {
        actions.add(action);
    }
}
