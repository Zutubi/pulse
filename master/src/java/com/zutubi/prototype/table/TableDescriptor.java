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
     * Heading for this table, already i18n'd.
     */
    private String heading;
    private boolean addAllowed;
    /**
     * The columns descriptors associated with this table descriptor.
     */
    private List<ColumnDescriptor> columns = new LinkedList<ColumnDescriptor>();

    private ActionDescriptor actions = null;

    public TableDescriptor(String heading, boolean addAllowed)
    {
        this.heading = heading;
        this.addAllowed = addAllowed;
    }

    public String getHeading()
    {
        return heading;
    }

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

    public List<String> getActions(Object configInstance)
    {
        return actions.getActions(configInstance);
    }

    public void addActionDescriptor(ActionDescriptor ad)
    {
        this.actions = ad;
    }

    public boolean isAddAllowed()
    {
        return addAllowed;
    }
}
