package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.type.record.Record;

import java.util.Arrays;
import java.util.List;

/**
 *
 *
 */
public class SingleRowDescriptor extends RowDescriptor
{
    public List<Row> instantiate(Record record)
    {
        Row row = new Row();
        
        for (ColumnDescriptor c : getColumnDescriptors())
        {
            Column column = c.instantiate(record);
            row.addCell(column);
        }

        return Arrays.asList(row);
    }
}
