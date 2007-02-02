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

    public void addColumn(Column column)
    {
        columns.add(column);
    }
}
