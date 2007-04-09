package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.record.Record;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class CollectionRowDescriptor extends RowDescriptor
{
    private CollectionType type;

    public CollectionRowDescriptor(CollectionType type)
    {
        this.type = type;
    }

    public List<Row> instantiate(Record record)
    {
        List<Row> rows = new LinkedList<Row>();

        if (record == null || record.size() == 0)
        {
            return addEmptyRow(rows);
        }

        int rowIndex = 1;
        for (String key : type.getOrder(record))
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
