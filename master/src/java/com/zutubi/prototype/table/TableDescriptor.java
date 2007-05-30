package com.zutubi.prototype.table;

import java.util.LinkedList;
import java.util.List;

/**
 * The table descriptor represents the model used to render a table to the UI.
 *
 *
 */
public class TableDescriptor
{
    /**
     * The columns descriptors associated with this table descriptor.
     */
    private List<ColumnDescriptor> columns = new LinkedList<ColumnDescriptor>();

    private List<String> actions = new LinkedList<String>();

    /**
     * Retreive the list of column descriptors associated with this table descriptor.
     *
     * @return a list of column descriptors.
     */
    public List<ColumnDescriptor> getColumns()
    {
        return new LinkedList<ColumnDescriptor>(columns);
    }

    /**
     * Add a column descriptor to this table descriptor.
     * 
     * @param descriptor
     */
    public void addColumn(ColumnDescriptor descriptor)
    {
        columns.add(descriptor);
    }

    public List<String> getActions()
    {
        return actions;
    }

    public void addAction(String action)
    {
        actions.add(action);
    }
}
