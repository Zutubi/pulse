package com.zutubi.prototype;

import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.record.Record;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 *
 *
 */
public class ListRowDescriptor extends RowDescriptor
{
    public List<Row> instantiate(Object value)
    {
        List<Row> rows = new LinkedList<Row>();

        Record record = (Record) value;
        if (record.size() == 0)
        {
            Column col = new Column();
            col.setValue("no data available.");
            col.setSpan(getColumnDescriptors().size());
            Row row = new Row();
            row.addCell(col);
            rows.add(row);
            return rows;            
        }

        // i really need the ListType instance here so that i can use its unstantiate method.
        List<Integer> keys = new LinkedList<Integer>();
        for (String key : record.keySet())
        {
            keys.add(Integer.valueOf(key));
        }
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++)
        {
            Integer key = keys.get(i);
            Object item = record.get(String.valueOf(key));

            Row row = new Row();
            row.setIndex(i);
            for (ColumnDescriptor c : getColumnDescriptors())
            {
                Column column = c.instantiate(item);
                column.addParameter("key", String.valueOf(key));
                row.addCell(column);
            }
            rows.add(row);
        }

        return rows;
    }
}
