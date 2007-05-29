package com.zutubi.prototype.model;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class Table extends UIComponent
{
    private List<Row> rows = new LinkedList<Row>();

    public void addRow(Row row)
    {
        rows.add(row);
    }

    public void addRows(List<Row> rows)
    {
        this.rows.addAll(rows);
    }

    public List<Row> getRows()
    {
        return rows;
    }
}
