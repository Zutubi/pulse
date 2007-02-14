package com.zutubi.prototype;

import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.model.Column;
import com.zutubi.pulse.prototype.record.Record;

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

        if (value instanceof List)
        {
            handleList(value, rows);
        }
        else
        {
            Record record = (Record) value;

            List<Object> list = new LinkedList<Object>();
            for (int i = 0; i < record.size(); i++)
            {
                Object o = record.get(String.valueOf(i));
                list.add(o);
            }
            handleList(list, rows);
        }
        return rows;
    }

    private void handleList(Object value, List<Row> rows)
    {
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
    }
}
