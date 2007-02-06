package com.zutubi.prototype.form.model;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class Table
{
    private List<Column> columns = new LinkedList<Column>();

    private List<Row> rows = new LinkedList<Row>();

    public void addColumn(Column column)
    {
        columns.add(column);
    }

    public List<Column> getColumns()
    {
        return columns;
    }

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

    public int getColspan()
    {
        return columns.size();
    }
}
