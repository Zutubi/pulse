package com.zutubi.pulse.form.descriptor;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class DefaultTableDescriptor implements TableDescriptor
{
    private List<ColumnDescriptor> columnDescriptors;

    public List<ColumnDescriptor> getColumnDescriptors()
    {
        return columnDescriptors;
    }

    public void setColumnDescriptors(List<ColumnDescriptor> columnDescriptors)
    {
        this.columnDescriptors = columnDescriptors;
    }

    public void addColumnDescriptor(ColumnDescriptor descriptor)
    {
        if (columnDescriptors == null)
        {
            columnDescriptors = new LinkedList<ColumnDescriptor>();
        }
        columnDescriptors.add(descriptor);
    }
}
