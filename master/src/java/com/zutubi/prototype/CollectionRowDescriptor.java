package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.type.record.Record;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class CollectionRowDescriptor extends RowDescriptor
{
    public List<Row> instantiate(Record record)
    {
        List<Row> rows = new LinkedList<Row>();

        if (record == null)
        {
            return addEmptyRow(rows);
        }

        String collectionOrder = record.getMeta("order");

        int rowIndex = 1;
        String[] keys = collectionOrder.split(",");
        for (String key : keys)
        {
            Record entry = (Record) record.get(key);

            Row row = new Row();
            row.setIndex(rowIndex);
            for (ColumnDescriptor c : getColumnDescriptors())
            {
                Column column = c.instantiate(entry);
                column.addParameter("key", key);
                row.addCell(column);
            }
            rows.add(row);
            
            rowIndex++;

        }

        return rows;
    }
}
