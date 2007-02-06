package com.zutubi.prototype.form;

import com.zutubi.prototype.form.model.Row;
import com.zutubi.prototype.form.model.Column;

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

    private int colspan = 1;

    public void setColspan(int colspan)
    {
        this.colspan = colspan;
    }

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
