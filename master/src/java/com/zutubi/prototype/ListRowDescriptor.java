package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.model.Row;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class ListRowDescriptor extends RowDescriptor
{
    public List<Row> instantiate(Object value)
    {
        List<Row> rows = new LinkedList<Row>();

        List<Object> listValue = null;
        if (value instanceof List)
        {
            listValue = (List<Object>)value;
        }
        else if (value instanceof Map)
        {
            listValue = new LinkedList<Object>(((Map)value).values());
        }

        if (value == null || listValue.size() == 0)
        {
            return addEmptyRow(rows);
        }

        for (int i = 0; i < listValue.size(); i++)
        {
            Object item = listValue.get(i);

            Row row = new Row();
            row.setIndex(i);
            for (ColumnDescriptor c : getColumnDescriptors())
            {
                Column column = c.instantiate(item);
                column.addParameter("key", String.valueOf(i));
                row.addCell(column);
            }
            rows.add(row);
        }

        return rows;
    }

    private List<Row> addEmptyRow(List<Row> rows)
    {
        Column col = new Column();
        col.setValue("no data available.");
        col.setSpan(getColumnDescriptors().size());
        Row row = new Row();
        row.addCell(col);
        rows.add(row);
        return rows;
    }
}
