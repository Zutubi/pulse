package com.zutubi.prototype;

import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.model.Column;

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
        if (list == null || list.size() == 0)
        {
            Column col = new Column();
            col.setValue("no data available.");
            col.setSpan(getColumnDescriptors().size());
            Row row = new Row();
            row.addCell(col);
            rows.add(row);
        }
        else
        {
            for (int i = 0; i < list.size(); i++)
            {
                Object item = list.get(i);
                Row row = new Row();
                row.setIndex(i);
                for (ColumnDescriptor c : getColumnDescriptors())
                {
                    Column column = c.instantiate(item);
                    row.addCell(column);
                }
                rows.add(row);
            }
        }
        return rows;
    }
}
