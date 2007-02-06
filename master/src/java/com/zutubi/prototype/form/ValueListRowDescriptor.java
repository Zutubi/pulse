package com.zutubi.prototype.form;

import com.zutubi.prototype.form.model.Row;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class ValueListRowDescriptor extends RowDescriptor
{
    public List<Row> instantiate(Object value)
    {
        List<Row> rows = new LinkedList<Row>();

        List<Object> list = (List<Object>) value;
        for (int i = 0; i < list.size(); i++)
        {
            Object item = list.get(i);
            Row row = new Row();
            row.setIndex(i);
            for (ColumnDescriptor c : getColumnDescriptors())
            {
                row.addCell(c.instantiate(item));
            }
            rows.add(row);
        }
        return rows;
    }
}
