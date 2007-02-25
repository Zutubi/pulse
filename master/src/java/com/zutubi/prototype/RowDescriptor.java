package com.zutubi.prototype;

import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.model.Column;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 *
 *
 */
public class RowDescriptor
{
    private List<ColumnDescriptor> columnDescriptors = new LinkedList<ColumnDescriptor>();

    public List<Row> instantiate(Object value)
    {
        Row row = new Row();
        for (ColumnDescriptor columnDescriptor : columnDescriptors)
        {
            Column column = columnDescriptor.instantiate(value);
            row.addCell(column);
        }
        return Arrays.asList(row);
    }

    public void addDescriptor(ColumnDescriptor columnDescriptor)
    {
        columnDescriptors.add(columnDescriptor);
    }

    public List<ColumnDescriptor> getColumnDescriptors()
    {
        return columnDescriptors;
    }
}
