package com.zutubi.prototype;

import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.record.Record;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 *
 *
 */
public abstract class RowDescriptor
{
    private List<ColumnDescriptor> columnDescriptors = new LinkedList<ColumnDescriptor>();

    public void addDescriptor(ColumnDescriptor columnDescriptor)
    {
        columnDescriptors.add(columnDescriptor);
    }

    public List<ColumnDescriptor> getColumnDescriptors()
    {
        return columnDescriptors;
    }

    protected List<Row> addEmptyRow(List<Row> rows)
    {
        Column col = new Column();
        col.setValue("no data available.");
        col.setSpan(getColumnDescriptors().size());
        Row row = new Row();
        row.addCell(col);
        rows.add(row);
        return rows;
    }

    public abstract List<Row> instantiate(Record record);
}
