package com.zutubi.prototype;

import com.zutubi.prototype.model.Row;
import com.zutubi.prototype.model.Column;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 *
 */
public class MapRowDescriptor extends RowDescriptor
{
    public List<Row> instantiate(Object value)
    {
        List<Row> rows = new LinkedList<Row>();

        Map<String, Object> mapValue = (Map<String, Object>) value;
        if (mapValue == null || mapValue.size() == 0)
        {
            return addEmptyRow(rows);
        }

        int i = 1;
        for (String key : mapValue.keySet())
        {
            Object item = mapValue.get(key);
            Row row = new Row();
            row.setIndex(i);
            for (ColumnDescriptor c : getColumnDescriptors())
            {
                Column column = c.instantiate(item);
                column.addParameter("key", key);
                row.addCell(column);
            }
            rows.add(row);
            i++;
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
